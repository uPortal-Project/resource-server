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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.resourceserver.aggr.om.Included;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProvider;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProviderUtils;

/**
 * Pays attention to the state of skin aggregation and only applies the fitler if it is disabled
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AggregationAwareFilterBean implements Filter {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private Filter filter;
    private ResourcesElementsProvider resourcesElementsProvider;

    /**
     * The filter to delegate to
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    /**
     * ResourcesElementsProvider, if not set {@link ResourcesElementsProviderUtils#getOrCreateResourcesElementsProvider(javax.servlet.ServletContext)} is used.
     */
    public void setResourcesElementsProvider(ResourcesElementsProvider resourcesElementsProvider) {
        this.resourcesElementsProvider = resourcesElementsProvider;
    }

    @Override
    public void destroy() {
        this.filter.destroy();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (this.resourcesElementsProvider == null) {
            final ServletContext servletContext = filterConfig.getServletContext();
            this.resourcesElementsProvider = ResourcesElementsProviderUtils.getOrCreateResourcesElementsProvider(servletContext);
        }
        
        this.filter.init(filterConfig);
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        final Included includedType = this.resourcesElementsProvider.getIncludedType(httpServletRequest);
        if (includedType == Included.AGGREGATED) {
            if (logger.isDebugEnabled()) {
                logger.debug("Aggregation enabled, delegating to filter: " + this.filter);
            }
            this.filter.doFilter(request, response, chain);
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Aggregation disabled, skipping filter: " + this.filter);
            }
            chain.doFilter(request, response);
        }
    }
}
