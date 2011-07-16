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

package org.jasig.resourceserver.aggr;

import java.io.File;

import org.jasig.resourceserver.aggr.om.BasicInclude;
import org.jasig.resourceserver.aggr.om.Included;
import org.jasig.resourceserver.aggr.om.Resources;

/**
 * DAO for reading and writing {@link Resources} objects to files.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface ResourcesDao {
    public static final String AGGREGATED_SKIN_SUB_SUFFIX = ".aggr.";
    
    /**
     * Write the specified {@link Resources} object to the specified File as XML
     */
    public void writeResources(final Resources resources, final File file);
    
    /**
     * Load the specified XML file into a {@link Resources} object.
     */
    public Resources readResources(final File resourcesXml);
    
    /**
     * Load the specified XML file into a {@link Resources} and filter its contents based on the specified scope
     */
    public Resources readResources(final File resourcesXml, Included scope);
    
    public String getAggregatedSkinName(String skinXmlName);
    
    public boolean isAbsolute(BasicInclude include);
    
    public boolean isConditional(BasicInclude include);
}
