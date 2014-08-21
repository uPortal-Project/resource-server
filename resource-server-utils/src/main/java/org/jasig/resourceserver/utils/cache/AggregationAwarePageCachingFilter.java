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

package org.jasig.resourceserver.utils.cache;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.constructs.web.filter.SimpleCachingHeadersPageCachingFilter;

import org.jasig.resourceserver.aggr.om.Included;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProvider;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProviderUtils;

/**
 * Extension of Ehcache's {@link SimplePageCachingFilter} that only does page caching if {@link Included#AGGREGATED} is
 * set.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AggregationAwarePageCachingFilter extends SimpleCachingHeadersPageCachingFilter {
    private ResourcesElementsProvider resourcesElementsProvider;

    /**
     * ResourcesElementsProvider, if not set {@link ResourcesElementsProviderUtils#getOrCreateResourcesElementsProvider(javax.servlet.ServletContext)} is used.
     */
    public void setResourcesElementsProvider(ResourcesElementsProvider resourcesElementsProvider) {
        this.resourcesElementsProvider = resourcesElementsProvider;
    }
    
    @Override
    public void doInit(FilterConfig filterConfig) throws CacheException {
        super.doInit(filterConfig);
        
        if (this.resourcesElementsProvider == null) {
            final ServletContext servletContext = filterConfig.getServletContext();
            this.resourcesElementsProvider = ResourcesElementsProviderUtils.getOrCreateResourcesElementsProvider(servletContext);
        }
    }

    @Override
    protected boolean filterNotDisabled(HttpServletRequest httpRequest) {
        final Included includedType = this.resourcesElementsProvider.getIncludedType(httpRequest);
        return Included.AGGREGATED == includedType && super.filterNotDisabled(httpRequest);
    }
}
