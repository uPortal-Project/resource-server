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

package org.jasig.resource.aggr.mojo;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.jasig.resource.aggr.AggregationRequest;
import org.jasig.resource.aggr.ResourcesAggregator;
import org.jasig.resource.aggr.ResourcesAggregatorImpl;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractSkinResourcesAggregatorMojo extends AbstractMojo {

    /**
     * @parameter default-value="10000"
     */
    protected int cssLineBreakColumnNumber = 10000;
    /**
     * @parameter default-value="false"
     */
    protected boolean disableJsOptimizations = false;
    /**
     * @parameter default-value="true"
     */
    protected boolean displayJsWarnings = true;
    /**
     * @parameter default-value="10000"
     */
    protected int jsLineBreakColumnNumber = 10000;
    /**
     * @parameter default-value="true"
     */
    protected boolean obfuscateJs = true;
    /**
     * @parameter default-value="true"
     */
    protected boolean preserveAllSemiColons = true;
    /**
     * @parameter default-value="MD5"
     */
    protected String digestAlgorithm = "MD5";
    /**
     * @parameter expression="${encoding}" default-value="${project.build.sourceEncoding}"
     */
    protected String encoding;
    /**
     * @parameter expression="${encoding}" default-value="${project.build.directory}/${project.build.finalName}
     * @required
     */
    protected File baseOutputDirectory;
    /**
     * A directory to place aggregated javascript files, useful if multiple skins share the same JavaScript. (none by default)
     * 
     * @parameter
     */
    protected String sharedJavaScriptDirectory;
    /**
     * If true the aggregator will look in the target directory for source files as well as the source directory, useful
     * if content from an overlay needs to be aggregated.
     * 
     * @parameter default-value="false"
     */
    protected boolean useGeneratedSources = false;
    
    protected ResourcesAggregator createResourcesAggregator() {
        final Log log = this.getLog();
        final CommonsLogToMavenLog logWrapper = new CommonsLogToMavenLog(log);

        final ResourcesAggregatorImpl aggr = new ResourcesAggregatorImpl(logWrapper, this.encoding);
        
        aggr.setCssLineBreakColumnNumber(cssLineBreakColumnNumber);
        aggr.setDisableJsOptimizations(disableJsOptimizations);
        aggr.setDisplayJsWarnings(displayJsWarnings);
        aggr.setJsLineBreakColumnNumber(jsLineBreakColumnNumber);
        aggr.setObfuscateJs(obfuscateJs);
        aggr.setPreserveAllSemiColons(preserveAllSemiColons);
        aggr.setDigestAlgorithm(digestAlgorithm);
        
        return aggr;
    }

    protected void doAggregation(ResourcesAggregator aggr, File skinConfigurationFile, File skinOutputDirectory) throws IOException {
        final Log log = this.getLog();
        
        final AggregationRequest aggregationRequest = new AggregationRequest();
        aggregationRequest
            .setResourcesXml(skinConfigurationFile)
            .setOutputBaseDirectory(skinOutputDirectory);
        
        if (sharedJavaScriptDirectory != null) {
            final File fullSharedJavaScriptDirectory = new File(baseOutputDirectory, sharedJavaScriptDirectory);
            aggregationRequest.setSharedJavaScriptDirectory(fullSharedJavaScriptDirectory);
        }
        
        if (useGeneratedSources) {
            aggregationRequest.addAdditionalSourceDirectory(skinOutputDirectory);
        }
        
        log.debug("Aggregating: " + aggregationRequest);
        aggr.aggregate(aggregationRequest);
    }
}