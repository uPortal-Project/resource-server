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
package org.jasig.portal.resourceserver;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.management.ManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.jmx.support.MBeanServerFactoryBean;

@SpringBootApplication
@EnableCaching
public class ResourceServerWebappApplication {

	@Autowired
	private CacheManager cacheManager;

	public static void main(String[] args) {
		SpringApplication.run(ResourceServerWebappApplication.class, args);
	}

	@Bean
	public MBeanServerFactoryBean mbeanServer() {
		final MBeanServerFactoryBean rslt = new MBeanServerFactoryBean();
		rslt.setLocateExistingServerIfPossible(true);
		return rslt;
	}

	@Bean
	public MethodInvokingFactoryBean ehcacheRegisterMBeans() {
		final MethodInvokingFactoryBean rslt = new MethodInvokingFactoryBean();
		rslt.setTargetClass(ManagementService.class);
		rslt.setTargetMethod("registerMBeans");
		rslt.setArguments(cacheManager, mbeanServer().getObject(), true, true, true, true);
		return rslt;
	}

}
