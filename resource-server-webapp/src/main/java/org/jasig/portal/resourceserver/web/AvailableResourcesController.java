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
package org.jasig.portal.resourceserver.web;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Eric Dalquist
 */
@Controller
@RequestMapping("/resourses.html")
public class AvailableResourcesController {

    private static final String BASE_PATH = "/rs/";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ServletContext servletContext;

    protected ModelAndView render() throws Exception {

        final Map<String, Map<String, Map<String, Resource[]>>> libraries = new TreeMap<>();
        
        final Set<String> libraryPaths = servletContext.getResourcePaths(BASE_PATH);
        for (final String libraryPath : libraryPaths) {
            if (!libraryPath.endsWith("/")) {
                continue;
            }
            
            final String libraryName = libraryPath.substring(BASE_PATH.length(), libraryPath.length() - 1);
            if (!libraries.containsKey(libraryName)) {
                libraries.put(libraryName, new TreeMap<>());
            }
            final Map<String, Map<String, Resource[]>> versions = libraries.get(libraryName);
            
            
            final Set<String> versionPaths = servletContext.getResourcePaths(libraryPath);
            for (final String versionPath : versionPaths) {
                if (!versionPath.endsWith("/")) {
                    continue;
                }
                
                final String libraryVersion = versionPath.substring(libraryPath.length(), versionPath.length() - 1);
                if (!versions.containsKey(libraryVersion)) {
                    versions.put(libraryVersion, new TreeMap<>());
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
