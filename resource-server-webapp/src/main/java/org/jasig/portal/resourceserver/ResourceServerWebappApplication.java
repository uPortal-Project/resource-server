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
