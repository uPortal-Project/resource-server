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

package org.jasig.resourceserver.utils.taglib;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * JavaScriptMinificationTag minifies blocks of in-page javascript.  This
 * tag is designed to be used to wrap javascript only and should be placed inside
 * the <script/> tag.
 * 
 * This tag is aware of the Jasig resource aggregator system property convention
 * and will automatically disable minification when that property has been 
 * set to true.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class JavaScriptMinificationTag extends BodyTagSupport {

    private static final long serialVersionUID = 1950546842057709745L;

    protected static final String AGGREGATION_PROP_NAME = "org.jasig.resource.aggr.util.aggregated_theme";

    protected final Log log = LogFactory.getLog(getClass());

    private int lineBreakColumnNumber = 10000;

    private boolean obfuscate = true;
    private boolean preserveAllSemiColons = true;
    private boolean disableOptimizations = false;
    
    public void setLineBreakColumnNumber(int lineBreakColumnNumber) {
        this.lineBreakColumnNumber = lineBreakColumnNumber;
    }

    public void setObfuscate(boolean obfuscate) {
        this.obfuscate = obfuscate;
    }

    public void setPreserveAllSemiColons(boolean preserveAllSemiColons) {
        this.preserveAllSemiColons = preserveAllSemiColons;
    }

    public void setDisableOptimizations(boolean disableOptimizations) {
        this.disableOptimizations = disableOptimizations;
    }

    public int doAfterBody() throws JspException {
        try {
            BodyContent bc = getBodyContent();

            // get the bodycontent as string
            String body = bc.getString();

            // getJspWriter to output content
            JspWriter out = bc.getEnclosingWriter();
            if (body != null) {
                
                // if the portal is currently configured for aggregation, use 
                // YUICompressor to aggregate the javascript contained in the tag
                if (Boolean.parseBoolean(System.getProperty(AGGREGATION_PROP_NAME, "true"))) {
                    Reader reader = new StringReader(body);
                    JavaScriptCompressor jsCompressor = new JavaScriptCompressor(reader, new JavaScriptErrorReporterImpl());
                    jsCompressor.compress(out, lineBreakColumnNumber, obfuscate, false, preserveAllSemiColons, disableOptimizations);
                } else {
                    out.print(body);
                }

            }
            
        } catch (IOException ioe) {
            throw new JspException("Error:" + ioe.getMessage());
        }

        return SKIP_BODY;
    }

    protected class JavaScriptErrorReporterImpl implements ErrorReporter {
        public void error(String message, String sourceName, int line,
                String lineSource, int lineOffset) {
            StringBuilder mesg = new StringBuilder(
                    "JavaScriptCompressor ERROR, ");
            mesg.append("message: ").append(message);
            mesg.append(", sourceName: ").append(sourceName);
            mesg.append(", line: ").append(line);
            mesg.append(", lineSource: ").append(lineSource);
            mesg.append(", lineOffset: ").append(lineOffset);
            log.error(mesg);
        }

        public EvaluatorException runtimeError(String message,
                String sourceName, int line, String lineSource, int lineOffset) {
            error(message, sourceName, line, lineSource, lineOffset);
            return new EvaluatorException(message, sourceName, line,
                    lineSource, lineOffset);
        }

        public void warning(String message, String sourceName, int line,
                String lineSource, int lineOffset) {
            StringBuilder mesg = new StringBuilder(
                    "JavaScriptCompressor WARNING, ");
            mesg.append("message: ").append(message);
            mesg.append(", sourceName: ").append(sourceName);
            mesg.append(", line: ").append(line);
            mesg.append(", lineSource: ").append(lineSource);
            mesg.append(", lineOffset: ").append(lineOffset);
            log.warn(mesg);
        }
    }

}
