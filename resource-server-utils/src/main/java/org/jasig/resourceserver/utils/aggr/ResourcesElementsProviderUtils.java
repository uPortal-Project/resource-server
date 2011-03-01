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

package org.jasig.resourceserver.utils.aggr;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for finding and loading {@link ResourcesElementsProvider} instances
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class ResourcesElementsProviderUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourcesElementsProviderUtils.class);
    
    private ResourcesElementsProviderUtils() {
    }
    
    public static ResourcesElementsProvider getResourcesElementsProvider(final ServletContext servletContext) { 
        return (ResourcesElementsProvider)servletContext.getAttribute(ResourcesElementsProvider.RESOURCES_ELEMENTS_PROVIDER);
    }
    
    public static ResourcesElementsProvider getOrCreateResourcesElementsProvider(final ServletContext servletContext) { 
        //Lock to make sure only one ResourcesElementsProvider instance is created if need be
        synchronized (servletContext) {
            final ResourcesElementsProvider resourcesElementsProvider = getResourcesElementsProvider(servletContext);
            if (resourcesElementsProvider != null) {
                return resourcesElementsProvider;
            }
            
            LOGGER.warn("No ResourcesElementsProvider found as a ServletContext attribute. Creating a new ResourcesElementsProviderImpl and caching it in the ServletContext under key: {}", ResourcesElementsProvider.RESOURCES_ELEMENTS_PROVIDER);
            
            //Create a new resource elements provider
            final ResourcesElementsProviderImpl provider = new ResourcesElementsProviderImpl();
            provider.setServletContext(servletContext);
            try {
                provider.afterPropertiesSet();
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to create ResourcesElementsProviderImpl on demand", e);
            }
            servletContext.setAttribute(ResourcesElementsProvider.RESOURCES_ELEMENTS_PROVIDER, provider);
            
            return provider;
        }
    }
}
