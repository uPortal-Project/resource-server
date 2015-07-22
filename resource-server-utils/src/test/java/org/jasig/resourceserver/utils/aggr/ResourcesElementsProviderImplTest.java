/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.resourceserver.utils.aggr;

import static junit.framework.Assert.assertEquals;

import java.net.URL;

import javax.servlet.ServletContext;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ResourcesElementsProviderImplTest {
    @Test 
    public void testHtmlFragmentWithResourceServer() throws Exception {
        final ResourcesElementsProviderImpl resourcesElementsProvider = new ResourcesElementsProviderImpl();
        
        resourcesElementsProvider.setResourceLoader(new DefaultResourceLoader());
        
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockServletContext servletContext = new MockServletContext();
        resourcesElementsProvider.setServletContext(servletContext);
        resourcesElementsProvider.afterPropertiesSet();
        
        final ServletContext rsServletContext = Mockito.mock(ServletContext.class);
        Mockito
            .when(rsServletContext.getContextPath())
            .thenReturn(ResourcesElementsProvider.DEFAULT_RESOURCE_CONTEXT);
        Mockito
            .when(rsServletContext.getResource("/rs/jqueryui/1.6rc6/theme/smoothness/ui.all.min.css"))
            .thenReturn(new URL("file:/rs/jqueryui/1.6rc6/theme/smoothness/ui.all.min.css"));
        
        servletContext.registerContext(ResourcesElementsProvider.DEFAULT_RESOURCE_CONTEXT, rsServletContext);
        
        request.setContextPath("/uPortal");
        
        final String resourcesHtmlFragment = resourcesElementsProvider.getResourcesHtmlFragment(request, "skin.xml");
        
        assertEquals(
                "<link href=\"/common/css/fluid/RoOqeu2wDdFeYLKNM--aZw.aggr.min.css\" rel=\"stylesheet\" type=\"text/css\"/>" +
                "<link href=\"/common/css/5k3CfLpXv77hfe46aYQr0w.aggr.min.css\" rel=\"stylesheet\" type=\"text/css\"/>" +
                "<link href=\"/uPortal/2ayEwnDhvYkCRskggG9fGg.aggr.min.css\" rel=\"stylesheet\" type=\"text/css\"/>" +
                "<link href=\"/ResourceServingWebapp/rs/jqueryui/1.6rc6/theme/smoothness/ui.all.min.css\" rel=\"stylesheet\" type=\"text/css\"/>" +
                "<link href=\"/uPortal/HZiiPRKVMPA6teq0Amrffw.aggr.min.css\" rel=\"stylesheet\" type=\"text/css\"/>" +
                "<!--[if IE 6]> <link rel=\"stylesheet\" type=\"text/css\" href=\"/uPortal/-91Rz1Gl-OCL5poZBhmf5g.aggr.min.css\"/> <![endif]-->" +
                "<script src=\"/uPortal/XR-SqpcE6eDDI_uEp3vkCQ.aggr.min.js\" type=\"text/javascript\"> </script>", 
        		resourcesHtmlFragment);
    }
    
    @Test 
    public void testHtmlFragmentWithoutServer() throws Exception {
        final ResourcesElementsProviderImpl resourcesElementsProvider = new ResourcesElementsProviderImpl();
        
        resourcesElementsProvider.setResourceLoader(new DefaultResourceLoader());
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockServletContext servletContext = new MockServletContext();
        resourcesElementsProvider.setServletContext(servletContext);
        resourcesElementsProvider.afterPropertiesSet();
        
        request.setContextPath("/uPortal");
        
        final String resourcesHtmlFragment = resourcesElementsProvider.getResourcesHtmlFragment(request, "skin.xml");
      
        assertEquals(
                "<link href=\"/common/css/fluid/RoOqeu2wDdFeYLKNM--aZw.aggr.min.css\" rel=\"stylesheet\" type=\"text/css\"/>" +
                "<link href=\"/common/css/5k3CfLpXv77hfe46aYQr0w.aggr.min.css\" rel=\"stylesheet\" type=\"text/css\"/>" +
                "<link href=\"/uPortal/2ayEwnDhvYkCRskggG9fGg.aggr.min.css\" rel=\"stylesheet\" type=\"text/css\"/>" +
                "<link href=\"/uPortal/rs/jqueryui/1.6rc6/theme/smoothness/ui.all.min.css\" rel=\"stylesheet\" type=\"text/css\"/>" +
                "<link href=\"/uPortal/HZiiPRKVMPA6teq0Amrffw.aggr.min.css\" rel=\"stylesheet\" type=\"text/css\"/>" +
                "<!--[if IE 6]> <link rel=\"stylesheet\" type=\"text/css\" href=\"/uPortal/-91Rz1Gl-OCL5poZBhmf5g.aggr.min.css\"/> <![endif]-->" +
                "<script src=\"/uPortal/XR-SqpcE6eDDI_uEp3vkCQ.aggr.min.js\" type=\"text/javascript\"> </script>", 
                resourcesHtmlFragment);
    }
}
