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
package org.jasig.resourceserver.utils.filter;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import junit.framework.TestCase;

import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CacheExpirationFilterTest extends TestCase {
    private final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    private final ServletContext servletContext = Mockito.mock(ServletContext.class);
    
    private CacheExpirationFilter cacheExpirationFilter;
    
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        Mockito.when(filterConfig.getInitParameterNames()).thenReturn(new Enumeration<Object>() {
            @Override
            public boolean hasMoreElements() {
                return false;
            }
            @Override
            public Object nextElement() {
                return null;
            }
        });
        Mockito.when(filterConfig.getServletContext()).thenReturn(servletContext);
        Mockito.when(filterConfig.getFilterName()).thenReturn("CacheExpirationFilter");
        Mockito.when(servletContext.getContextPath()).thenReturn("/ResourceServingWebapp");
        
        this.cacheExpirationFilter = new CacheExpirationFilter();
        this.cacheExpirationFilter.init(filterConfig);
    }



    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        this.cacheExpirationFilter.destroy();
    }

    public void testTimezoneFormat() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain chain = new MockFilterChain();
        
        this.cacheExpirationFilter.doFilter(request, response, chain);
        
        final Long expires = (Long)response.getHeader("Expires");
        final String cacheControl = (String)response.getHeader("Cache-Control");
        
        assertTrue(expires > System.currentTimeMillis());
        assertEquals("public, max-age=31536000", cacheControl);
    }
}
