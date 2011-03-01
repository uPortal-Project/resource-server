/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.resourceserver.utils.taglib;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import junit.framework.TestCase;

import org.jasig.resourceserver.utils.aggr.ResourcesElementsProvider;
import org.mockito.Mockito;

/**
 * TestResourceIncludeTag provides unit tests for the resource inclusion
 * JSP tags.
 * 
 * @author Jen Bourey
 */
public class TestAggregatedResourcesTag extends TestCase {
	private static final String CURRENT_CONTEXT = "/TestContext";
	
	/*
	 * Create the ResourceIncludeTag to test, as well as necessary mock objects
	 */
	private ResourceIncludeTag tag = new ResourceIncludeTag();
	private final PageContext pageContext = Mockito.mock(PageContext.class);
	private final ServletContext servletContext = Mockito.mock(ServletContext.class);
	private final HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
	private final JspWriter jspWriter = Mockito.mock(JspWriter.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// set our tag to produce a mock ServletContext
		tag.setPageContext(pageContext);
		Mockito.when(pageContext.getServletContext()).thenReturn(servletContext);
		Mockito.when(pageContext.getRequest()).thenReturn(httpServletRequest);

		// give us a current context path
		Mockito.when(httpServletRequest.getContextPath()).thenReturn(CURRENT_CONTEXT);
		
		// set our mock JspWriter
		Mockito.when(pageContext.getOut()).thenReturn(jspWriter);

	}


	/**
	 * Test returning a local resource URL if the resource serving webapp
	 * context is unavailable.
	 * 
	 * @throws JspException
	 */
	public void testGetLocalUrl() throws JspException {
		tag.setValue("/test/resource");
		tag.doStartTag();
		
		assertEquals(CURRENT_CONTEXT.concat("/test/resource"), tag.getUrl());
		
	}
	
	/**
	 * Test returning a ResourceServingWebapp url with no init parameter
	 * in the web.xml to define the path.
	 * 
	 * @throws JspException
	 */
	public void testGetResourceWebappUrl() throws Exception {

		tag.setValue("/test/resource");

		Mockito
			.when(servletContext.getContext(ResourcesElementsProvider.DEFAULT_RESOURCE_CONTEXT))
			.thenReturn(servletContext);
		
		Mockito
            .when(servletContext.getContextPath())
            .thenReturn(ResourcesElementsProvider.DEFAULT_RESOURCE_CONTEXT);
		
		Mockito
            .when(servletContext.getResource("/test/resource"))
            .thenReturn(new URL("file:/test/resource"));
		
		tag.doStartTag();
		
		assertEquals(ResourcesElementsProvider.DEFAULT_RESOURCE_CONTEXT.concat("/test/resource"), tag.getUrl());

	}
	
	/**
	 * Test returning a ResourceServingWebapp url when the webapp's context 
	 * has been specified as a non-default url.
	 * 
	 * @throws JspException
	 */
	public void testGetOverriddenResourceUrl() throws Exception {

		tag.setValue("/test/resource");
		
		Mockito
			.when(servletContext.getInitParameter(ResourcesElementsProvider.RESOURCE_CONTEXT_INIT_PARAM))
			.thenReturn("/OverrideResourceWebapp");
		Mockito
			.when(servletContext.getContext("/OverrideResourceWebapp"))
			.thenReturn(servletContext);
        Mockito
            .when(servletContext.getContextPath())
            .thenReturn("/OverrideResourceWebapp");
        Mockito
            .when(servletContext.getResource("/test/resource"))
            .thenReturn(new URL("file:/test/resource"));
		
		tag.doStartTag();

		assertEquals("/OverrideResourceWebapp".concat("/test/resource"), tag.getUrl());

	}
	
	/**
	 * Ensure that if the tag gracefully handles missing trailing slashes from
	 * the resource webapp context or resource path strings.
	 * 
	 * @throws JspException
	 */
	public void testAddSlashesAsNecessary() throws Exception {
		
		Mockito.when(servletContext.getContext(ResourcesElementsProvider.DEFAULT_RESOURCE_CONTEXT))
			.thenReturn(servletContext);
        
        Mockito
            .when(servletContext.getContextPath())
            .thenReturn(ResourcesElementsProvider.DEFAULT_RESOURCE_CONTEXT);
        
        Mockito
            .when(servletContext.getResource("/test/resource"))
            .thenReturn(new URL("file:/test/resource"));

		// set both the resource string and the specified resource server context
		// without leading forward slashes
		tag.setValue("test/resource");
		Mockito.when(servletContext.getInitParameter(ResourcesElementsProvider.RESOURCE_CONTEXT_INIT_PARAM))
			.thenReturn(ResourcesElementsProvider.DEFAULT_RESOURCE_CONTEXT.substring(1));
		tag.doStartTag();
		
		// tag should properly handle missing slashes
		assertEquals(ResourcesElementsProvider.DEFAULT_RESOURCE_CONTEXT.concat("/test/resource"), tag.getUrl());
		
	}
	
	/**
	 * Ensure that the url is saved to the appropriate variable is set when 
	 * a variable name is specified.
	 * 
	 * @throws JspException
	 */
	public void testSetVariable() throws JspException {
		tag.setValue("/test/resource");
		tag.setVar("var");
		tag.doStartTag();
		tag.doEndTag();
		
		Mockito.verify(pageContext, Mockito.times(1)).setAttribute("var", CURRENT_CONTEXT.concat("/test/resource"));
	}

	/**
	 * Ensure that the url is printed out when no variable name is specified.
	 * 
	 * @throws JspException
	 * @throws IOException
	 */
	public void testUnSetVariable() throws JspException, IOException {
		tag.setValue("/test/resource");
		tag.doStartTag();
		tag.doEndTag();
		
		Mockito.verify(jspWriter, Mockito.times(1)).print(CURRENT_CONTEXT.concat("/test/resource"));
	}


}
