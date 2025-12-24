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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Stub class - no longer needed since YUI Compressor was replaced with esbuild
 * Kept for backward compatibility
 * 
 * @author Eric Dalquist
 * @version $Revision$
 * @deprecated No longer used - esbuild handles compression
 */
@Deprecated
public class CommonsLogErrorReporter {
    private final Log logger;
    
    public CommonsLogErrorReporter() {
        this.logger = LogFactory.getLog(this.getClass());
    }
    
    public CommonsLogErrorReporter(Log logger) {
        this.logger = logger;
    }
}
