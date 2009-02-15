/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.resourceserver.web;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.management.ManagementService;

/**
 * Calls {@link CacheManager#getInstance()} to get the {@link CacheManager} to register. Calls
 * {@link ManagementFactory#getPlatformMBeanServer()} to get the {@link MBeanServer} to register
 * with.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class EhCacheJmxRegistrationListener implements ServletContextListener {

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent sce) {
        final CacheManager cacheManager = CacheManager.getInstance();
        final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        ManagementService.registerMBeans(cacheManager, mBeanServer, true, true, true, true);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
