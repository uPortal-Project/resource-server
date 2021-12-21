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
package org.jasig.resourceserver.utils.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PathBasedCacheExpirationFilterTest {
    private final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    private final ServletContext servletContext = Mockito.mock(ServletContext.class);



    private PathBasedCacheExpirationFilter pathBasedCacheExpirationFilter;

    @Before
    public void setUp() throws Exception {
        Mockito.when(filterConfig.getInitParameterNames()).thenReturn(new Enumeration<String>() {
            @Override
            public boolean hasMoreElements() {
                return false;
            }
            @Override
            public String nextElement() {
                return null;
            }
        });
        Mockito.when(filterConfig.getServletContext()).thenReturn(servletContext);
        Mockito.when(filterConfig.getFilterName()).thenReturn("CacheExpirationFilter");
        Mockito.when(servletContext.getContextPath()).thenReturn("/ResourceServingWebapp");

        this.pathBasedCacheExpirationFilter = new PathBasedCacheExpirationFilter();
        this.pathBasedCacheExpirationFilter.init(filterConfig);
    }

    @After
    public void tearDown() throws Exception {
        this.pathBasedCacheExpirationFilter.destroy();
    }

    @Test
    public void testPathBasedCacheExpirationFilterHit() throws Exception {
        // Subtract 10 seconds to allow for slow test runs
        final long expectedExpires = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365) - 10000;

        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain chain = new MockFilterChain();

        request.setServletPath("/rs/jqueryui/1.8/jquery-ui-1.8.js");

        this.pathBasedCacheExpirationFilter.doFilter(request, response, chain);

        final long expires = response.getDateHeader("Expires");
        final String cacheControl = response.getHeader("Cache-Control");

        assertTrue(expires > expectedExpires);
        assertEquals("public, max-age=31536000", cacheControl);
    }

    @Test
    public void testPathBasedCacheExpirationFilterMiss() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain chain = new MockFilterChain();

        request.setServletPath("/index.html");

        this.pathBasedCacheExpirationFilter.doFilter(request, response, chain);

        final Long expires = (Long)response.getHeaderValue("Expires");
        final String cacheControl = response.getHeader("Cache-Control");

        assertNull(expires);
        assertNull(cacheControl);
    }
}
