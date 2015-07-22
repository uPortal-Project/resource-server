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
package org.jasig.resource.aggr;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Captures the options passed to {@link ResourcesAggregator#aggregate(AggregationRequest)}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AggregationRequest {
    private File resourcesXml;
    private File outputBaseDirectory;
    private File sharedJavaScriptDirectory;    
    private final List<File> additionalSourceDirectories = new LinkedList<File>();
    
    /**
     * @return the resources XML file to aggregate (required)
     */
    public File getResourcesXml() {
        return this.resourcesXml;
    }
    /**
     * @return the base directory to output files to (required)
     */
    public File getOutputBaseDirectory() {
        return this.outputBaseDirectory;
    }
    /**
     * @return a shared javascript directory to output all JS files to
     */
    public File getSharedJavaScriptDirectory() {
        return this.sharedJavaScriptDirectory;
    }
    /**
     * @return additional directory to look for resource files in
     */
    public List<File> getAdditionalSourceDirectories() {
        return this.additionalSourceDirectories;
    }
    
    
    public AggregationRequest setResourcesXml(File resourcesXml) {
        this.resourcesXml = resourcesXml;
        return this;
    }
    public AggregationRequest setOutputBaseDirectory(File outputBaseDirectory) {
        this.outputBaseDirectory = outputBaseDirectory;
        return this;
    }
    public AggregationRequest setSharedJavaScriptDirectory(File sharedJavaScriptDirectory) {
        this.sharedJavaScriptDirectory = sharedJavaScriptDirectory;
        return this;
    }
    public AggregationRequest addAdditionalSourceDirectory(File additionalSourceDirectory) {
        this.additionalSourceDirectories.add(additionalSourceDirectory);
        return this;
    }
    public AggregationRequest addAdditionalSourceDirectories(Collection<File> additionalSourceDirectories) {
        this.additionalSourceDirectories.addAll(additionalSourceDirectories);
        return this;
    }
    @Override
    public String toString() {
        return "[resourcesXml=" + this.resourcesXml + ", outputBaseDirectory="
                + this.outputBaseDirectory + ", sharedJavaScriptDirectory=" + this.sharedJavaScriptDirectory
                + ", additionalSourceDirectories=" + this.additionalSourceDirectories + "]";
    }
}
