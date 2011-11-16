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
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.resourceserver.aggr.om.Included;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProvider;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProviderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.GenericFilterBean;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * Sets Expires and Cache-Control "public, max-age" headers on resources based on their paths. Uses {@link AntPathMatcher}
 * to match paths.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PathBasedCacheExpirationFilter extends GenericFilterBean {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    final Map<String, Long> DEFAULT_CACHE_PATHS = ImmutableMap.of(
            "/**/*.aggr.min.js", TimeUnit.DAYS.toSeconds(365), 
            "/**/*.aggr.min.css", TimeUnit.DAYS.toSeconds(365),
            "/**/*.min.js", TimeUnit.DAYS.toSeconds(365),
            "/**/*.min.css", TimeUnit.DAYS.toSeconds(365),
            "/rs/**/*", TimeUnit.DAYS.toSeconds(365));

    private ResourcesElementsProvider resourcesElementsProvider;
    private Map<String, Long> cacheMaxAges;
    private Map<Long, String> cachedControlStrings;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public PathBasedCacheExpirationFilter() {
        this.setCacheMaxAges(DEFAULT_CACHE_PATHS);
    }
    
    
    /**
     * Specify map of ant paths to max-age times (in seconds). The default map is:
     * 
     * /**&#47;*.aggr.min.js - 365 days
     * /**&#47;*.aggr.min.css - 365 days
     * /**&#47;*.min.js - 365 days
     * /**&#47;*.min.css - 365 days
     * /rs/**&#47;* - 365 days
     */
    public void setCacheMaxAges(Map<String, ? extends Number> cacheMaxAges) {
        final Builder<String, Long> cacheMaxAgesBuilder = ImmutableMap.builder();
        final Builder<Long, String> cachedControlStringsBuilder = ImmutableMap.builder();
        final Set<Long> maxAgeLog = new HashSet<Long>();
        
        for (final Map.Entry<String, ? extends Number> cacheMaxAgeEntry : cacheMaxAges.entrySet()) {
            final Number maxAgeNum = cacheMaxAgeEntry.getValue();
            final int maxAge = maxAgeNum.intValue();
            final long maxAgeMillis = TimeUnit.SECONDS.toMillis(maxAge);
            cacheMaxAgesBuilder.put(cacheMaxAgeEntry.getKey(), maxAgeMillis);
            
            if (maxAgeLog.add(maxAgeMillis)) {
                cachedControlStringsBuilder.put(maxAgeMillis, "public, max-age=" + maxAge);
            }
        }
        
        this.cacheMaxAges = cacheMaxAgesBuilder.build();
        this.cachedControlStrings = cachedControlStringsBuilder.build();
    }

    /**
     * ResourcesElementsProvider, if not set {@link ResourcesElementsProviderUtils#getOrCreateResourcesElementsProvider(javax.servlet.ServletContext)} is used.
     */
    public void setResourcesElementsProvider(ResourcesElementsProvider resourcesElementsProvider) {
        this.resourcesElementsProvider = resourcesElementsProvider;
    }

    /* (non-Javadoc)
     * @see org.springframework.web.filter.GenericFilterBean#initFilterBean()
     */
    @Override
    protected void initFilterBean() throws ServletException {
        if (this.resourcesElementsProvider == null) {
            final ServletContext servletContext = this.getServletContext();
            this.resourcesElementsProvider = ResourcesElementsProviderUtils.getOrCreateResourcesElementsProvider(servletContext);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        // add the cache expiration time to the response
        if (response instanceof HttpServletResponse && request instanceof HttpServletRequest) {
            final HttpServletRequest httpServletRequest = (HttpServletRequest)request;
            final Included includedType = this.resourcesElementsProvider.getIncludedType(httpServletRequest);
            if (includedType == Included.AGGREGATED) {
                final HttpServletResponse httpResponse = (HttpServletResponse) response;
    
                final String path = ((HttpServletRequest) request).getServletPath();
    
                for (final Entry<String, Long> entry : this.cacheMaxAges.entrySet()) {
    
                    if (this.pathMatcher.match(entry.getKey(), path)) {
    
                        final Long maxAge = entry.getValue();
                        
                        httpResponse.setDateHeader("Expires", System.currentTimeMillis() + maxAge);
                        httpResponse.setHeader("Cache-Control", this.cachedControlStrings.get(maxAge));
    
                        break;
                    }
                }
            }
        }

        // continue
        chain.doFilter(request, response);
    }
}
