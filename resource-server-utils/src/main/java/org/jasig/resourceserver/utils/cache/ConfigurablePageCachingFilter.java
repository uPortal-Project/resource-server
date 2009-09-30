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

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.constructs.web.filter.SimplePageCachingFilter;

/**
 * ConfigurablePageCachingFilter provides a subclass of ehCache's 
 * SimplePageCachingFilter that allows configuration of the cache name
 * and cache manager.
 * 
 * @author Jen Bourey
 */
public class ConfigurablePageCachingFilter extends SimplePageCachingFilter {

	private static final String DEFAULT_CACHE_NAME = 
		"org.jasig.resourceserver.utils.cache.ConfigurablePageCachingFilter.PAGE_CACHE";
	
	private final CacheManager cacheManager;
	private final String cacheName;
	
	/**
	 * 
	 * @param cacheManager
	 */
	public ConfigurablePageCachingFilter(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
		this.cacheName = DEFAULT_CACHE_NAME;
	}
	
	/**
	 * 
	 * @param cacheManager
	 * @param cacheName
	 */
	public ConfigurablePageCachingFilter(CacheManager cacheManager, String cacheName) {
		this.cacheManager = cacheManager;
		this.cacheName = cacheName;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @Override
	 */
	@Override
    protected CacheManager getCacheManager() {
		return cacheManager;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @Override
	 */
    @Override
	public String getCacheName() {
		return cacheName;
	}
	
}
