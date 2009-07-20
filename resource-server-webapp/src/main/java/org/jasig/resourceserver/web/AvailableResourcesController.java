/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.resourceserver.web;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AvailableResourcesController extends AbstractController {
    private static final String BASE_PATH = "/rs/";
    

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final ApplicationContext applicationContext = this.getApplicationContext();
        final ServletContext servletContext = this.getServletContext();
        
        final Map<String, Map<String, Map<String, Resource[]>>> libraries = new TreeMap<String, Map<String, Map<String, Resource[]>>>();
        
        final Set<String> libraryPaths = servletContext.getResourcePaths(BASE_PATH);
        for (final String libraryPath : libraryPaths) {
            if (!libraryPath.endsWith("/")) {
                continue;
            }
            
            final String libraryName = libraryPath.substring(BASE_PATH.length(), libraryPath.length() - 1);
            if (!libraries.containsKey(libraryName)) {
                libraries.put(libraryName, new TreeMap<String, Map<String, Resource[]>>());
            }
            final Map<String, Map<String, Resource[]>> versions = libraries.get(libraryName);
            
            
            final Set<String> versionPaths = servletContext.getResourcePaths(libraryPath);
            for (final String versionPath : versionPaths) {
                if (!versionPath.endsWith("/")) {
                    continue;
                }
                
                final String libraryVersion = versionPath.substring(libraryPath.length(), versionPath.length() - 1);
                if (!versions.containsKey(libraryVersion)) {
                    versions.put(libraryVersion, new TreeMap<String, Resource[]>());
                }
                final Map<String, Resource[]> resources = versions.get(libraryVersion);
                
                final Resource[] jsResources = applicationContext.getResources(versionPath + "**/*.js");
                resources.put("js", jsResources);
                
                final Resource[] cssResources = applicationContext.getResources(versionPath + "**/*.css");
                resources.put("css", cssResources);
                
            }
        }
        
        return new ModelAndView("availableResources", "libraries", libraries);
    }
}
