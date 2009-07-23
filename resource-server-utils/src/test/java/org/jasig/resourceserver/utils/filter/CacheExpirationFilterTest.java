/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.resourceserver.utils.filter;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import junit.framework.TestCase;

import org.mockito.Mockito;

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
        Mockito.when(filterConfig.getInitParameterNames()).thenReturn(new Enumeration<?>() {
            public boolean hasMoreElements() {
                return false;
            }
            public Object nextElement() {
                return null;
            }
        });
        Mockito.when(filterConfig.getServletContext()).thenReturn(servletContext);
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
        final String expiresHeader = this.cacheExpirationFilter.getExpiresHeader();
        assertNotNull(expiresHeader);
        assertTrue(expiresHeader.endsWith(" GMT"));
    }
}
