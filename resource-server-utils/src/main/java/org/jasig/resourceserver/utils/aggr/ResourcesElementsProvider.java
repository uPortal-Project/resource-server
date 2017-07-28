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
package org.jasig.resourceserver.utils.aggr;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.jasig.resourceserver.aggr.om.Included;
import org.jasig.resourceserver.aggr.om.Resources;
import org.w3c.dom.NodeList;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface ResourcesElementsProvider {
    /**
     * Attribute to store a ResourcesElementsProvider instance with as a {@link HttpServletRequest} or {@link ServletContext attribute} 
     */
    public static final String RESOURCES_ELEMENTS_PROVIDER = ResourcesElementsProvider.class.getName();
    
    /**
     * System property used to determine if aggregation is enabled or not 
     */
    public static final String AGGREGATED_THEME_PARAMETER = ResourcesElementsProvider.class.getPackage().getName() + ".aggregated_theme";
    public static final String LEGACY_AGGREGATED_THEME_PARAMETER_1 = "org.jasig.resource.aggr.util.aggregated_theme";
    public static final String LEGACY_AGGREGATED_THEME_PARAMETER_2 = "org.jasig.portal.web.skin.aggregated_theme";
    
    /**
     * Default value of the {@link #AGGREGATED_THEME_PARAMETER} system property
     */
    public static final String DEFAULT_AGGREGATION_ENABLED = Boolean.TRUE.toString();

    /**
     * Servlet context init-param used to specify the context path of the Resource Server
     */
    public static final String RESOURCE_CONTEXT_INIT_PARAM = "resourceContextPath";
    
    /**
     * Default context path used for the Resource Server if no {@link #RESOURCE_CONTEXT_INIT_PARAM} is specified 
     */
    public static final String DEFAULT_RESOURCE_CONTEXT = "/ResourceServingWebapp";
    
    /**
     * Resolve the full path to the specified resource based on the availability of the resource-server.
     * 
     * The resource server is looked up using {@link ServletContext#getContext(String)}. The context name
     * is resolved from the {@link ResourcesElementsProvider#RESOURCE_CONTEXT_INIT_PARAM} init param and if
     * that is not set {@link ResourcesElementsProvider#DEFAULT_RESOURCE_CONTEXT} is used.
     * 
     * If the resource server is available and contains the requested resource 
     */
    public String resolveResourceUrl(HttpServletRequest request, String resource);

    /**
     * Set the default include type for resources. {@link Included#PLAIN} results in un-aggregated
     * resources being returned. {@link Included#AGGREGATED} results in aggregated resources being
     * returned. {@link Included#BOTH} is not supported as an argument here.
     */
    public void setDefaultIncludedType(Included included);
    
    /**
     * They type of resources to be returned by default
     */
    public Included getDefaultIncludedType();
    
    /**
     * They type of resources to be returned for this request
     */
    public Included getIncludedType(HttpServletRequest request);

    /**
     * Get the specified skin parameter, null if there is no parameter with the specified name
     */
    public String getResourcesParameter(HttpServletRequest request, String skinXml, String name);

    /**
     * Get an XML NodeList of link and script tags for the specified skin. Uses {@link #getIncludedType(HttpServletRequest)}
     * to determine which resource URLs to use.
     */
    public NodeList getResourcesXmlFragment(HttpServletRequest request, String skinXml);
    
    /**
     * Get an HTML fragment of the link and script tags for the specified skin. Uses {@link #getIncludedType(HttpServletRequest)}
     * to determine which resource URLs to use.
     */
    public String getResourcesHtmlFragment(HttpServletRequest request, String skinXml);
    
    /**
     * Get the resources to use for rhte specified request and skin XML file. Uses {@link #getIncludedType(HttpServletRequest)}
     * to determine which resource URLs to use.
     */
    public Resources getResources(HttpServletRequest request, String skinXml);

}
