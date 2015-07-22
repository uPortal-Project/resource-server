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
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

/**
 * YUI compressor error reporter that delegates to commons-logging
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CommonsLogErrorReporter implements ErrorReporter {
    private final Log logger;
    
    public CommonsLogErrorReporter() {
        this.logger = LogFactory.getLog(this.getClass());
    }
    
    public CommonsLogErrorReporter(Log logger) {
        this.logger = logger;
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.ErrorReporter#error(java.lang.String, java.lang.String, int, java.lang.String, int)
     */
    @Override
    public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
        logger.error(
                "JavaScriptCompressor: " + message + 
                    ", sourceName: " + sourceName + 
                    ", line: " + line + 
                    ", lineSource: " + lineSource + 
                    ", lineOffset: " + lineOffset);
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.ErrorReporter#runtimeError(java.lang.String, java.lang.String, int, java.lang.String, int)
     */
    @Override
    public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
        error(message, sourceName, line, lineSource, lineOffset);
        return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.ErrorReporter#warning(java.lang.String, java.lang.String, int, java.lang.String, int)
     */
    @Override
    public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
        logger.warn(
                "JavaScriptCompressor: " + message + 
                    ", sourceName: " + sourceName + 
                    ", line: " + line + 
                    ", lineSource: " + lineSource + 
                    ", lineOffset: " + lineOffset);
    }

}
