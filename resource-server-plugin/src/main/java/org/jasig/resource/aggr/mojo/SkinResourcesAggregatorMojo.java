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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jasig.resource.aggr.ResourcesAggregator;
import org.jasig.resourceserver.aggr.AggregationException;

/**
 * Maven {@link AbstractMojo} to invoke {@link ResourcesAggregator#aggregate(File, File)}.
 * 
 * You must specify the skinConfigurationFile property, points to the "skin.xml"
 * file you wish to aggregate.
 *
 * @goal aggregate
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class SkinResourcesAggregatorMojo extends AbstractSkinResourcesAggregatorMojo {
	/**
	 * @parameter 
	 * @required
	 */
	private File skinConfigurationFile;
	
	/**
	 * @parameter
	 */
	private String skinOutputDirectory = "";
    
	/**
	 * @parameter expression="${encoding}" default-value="${project.build.directory}/${project.build.finalName}
	 */
	private File baseOutputDirectory;

	/* (non-Javadoc)
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
	    try {
			final ResourcesAggregator aggr = this.createResourcesAggregator();
			
			final File fullOutputDirectory = new File(baseOutputDirectory, skinOutputDirectory);
			this.doAggregation(aggr, skinConfigurationFile, fullOutputDirectory);
		} catch (AggregationException e) {
			throw new MojoExecutionException("aggregation failed", e);
		} catch (IOException e) {
			throw new MojoExecutionException("IOException occurred", e);
		}
	}
}
