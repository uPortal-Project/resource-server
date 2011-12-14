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
package org.jasig.resource.aggr;

import java.io.File;
import java.io.IOException;

import org.jasig.resourceserver.aggr.AggregationException;
import org.jasig.resourceserver.aggr.om.Resources;

/**
 * Interface defines operations for aggregating {@link Resources}.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 */
public interface ResourcesAggregator {

	/**
	 * @param resourcesXml The source resources file
     * @param outputBaseDirectory The file to write the output to
     * @see #aggregate(AggregationRequest)
	 */
	public void aggregate(File resourcesXml, File outputBaseDirectory) throws IOException, AggregationException;
	
	/**
     * @param resourcesXml The source resources file
     * @param outputBaseDirectory The file to write the output to
     * @param sharedJavaScriptDirectory Optional settings of a location to write all aggregated JavaScript to
     * @see #aggregate(AggregationRequest)
     */
    public void aggregate(File resourcesXml, File outputBaseDirectory, File sharedJavaScriptDirectory) throws IOException, AggregationException;
    
    /**
     * Aggregate the {@link Resources} object from the first {@link File} argument, placing
     * all generated CSS and Javascript in the directory denoted in the second {@link File} argument.
     * 
     * Will generate an aggregated version of the resourcesXml file in the outputBaseDirectory, with the filename
     * similar to the resourcesXml.
     * Example:
     * 
     * resourcesXml filename: skin.xml; output filename: skin.aggr.xml
     * 
     * @param aggregationRequest Details what to aggregate
     */
    public void aggregate(AggregationRequest aggregationRequest) throws IOException, AggregationException;
}
