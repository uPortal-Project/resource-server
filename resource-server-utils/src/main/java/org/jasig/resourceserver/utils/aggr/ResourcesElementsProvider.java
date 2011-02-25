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

import javax.servlet.http.HttpServletRequest;

import org.jasig.resourceserver.aggr.om.Included;
import org.w3c.dom.NodeList;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface ResourcesElementsProvider {
    
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

}