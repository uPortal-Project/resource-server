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

/**
 * 
 */
package org.jasig.resource.aggr.mojo;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.DirectoryScanner;
import org.jasig.resource.aggr.ResourcesAggregator;
import org.jasig.resourceserver.aggr.AggregationException;

/**
 * Maven {@link AbstractMojo} to invoke {@link ResourcesAggregator#aggregate(File, File)}.
 * 
 * You must specify the skinConfigurationFile property, points to the "skin.xml"
 * file you wish to aggregate.
 *
 * @goal batch-aggregate
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class BatchSkinResourcesAggregatorMojo extends AbstractSkinResourcesAggregatorMojo {
    /**
     * Directory containing skin files, defaults to the Maven Web application sources directory (src/main/webapp)
     *
     * @parameter default-value="${basedir}/src/main/webapp" 
     * @required
     */
    private File skinSourceDirectory;
    
    /**
     * Defines files in the source directories to include (none by default), recommended to be
     * set in favor of skinConfigurationFile
     * 
     * Example: "skins/**&#47;skin.xml"
     *
     * @parameter
     * @required
     */
    private String[] includes;
 
    /**
     * Defines which of the included files in the source directories to exclude (none by default).
     *
     * @parameter
     */
    private String[] excludes;
    
    /* (non-Javadoc)
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Log log = this.getLog();

        try {
            final ResourcesAggregator aggr = this.createResourcesAggregator();
            
            final Set<String> skinConfigurationFiles = this.findSkinConfigurationFiles();
            for (final String fileName : skinConfigurationFiles) {
                log.info("Aggregating: " + fileName);
                final File skinConfigurationFile = new File(this.skinSourceDirectory, fileName);
                final File skinOutputDirectory = new File(this.baseOutputDirectory, fileName).getParentFile();
                this.doAggregation(aggr, skinConfigurationFile, skinOutputDirectory);
            }
            
        } catch (AggregationException e) {
            throw new MojoExecutionException("aggregation failed", e);
        } catch (IOException e) {
            throw new MojoExecutionException("IOException occurred", e);
        }
    }

    private Set<String> findSkinConfigurationFiles() {
        final DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.setIncludes(includes);
        directoryScanner.setExcludes(excludes);
        directoryScanner.setBasedir(skinSourceDirectory);
        directoryScanner.scan();
        
        final Set<String> skinConfigurationFiles = new LinkedHashSet<String>();
        for (final String fileName : directoryScanner.getIncludedFiles()) {
            skinConfigurationFiles.add(fileName);
        }  
        
        return skinConfigurationFiles;
    }
}
