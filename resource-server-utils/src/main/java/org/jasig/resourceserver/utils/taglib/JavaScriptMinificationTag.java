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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.resource.aggr.CommonsLogErrorReporter;
import org.jasig.resourceserver.aggr.om.Included;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProvider;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProviderUtils;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import com.yahoo.platform.yui.org.mozilla.javascript.ErrorReporter;
import com.yahoo.platform.yui.org.mozilla.javascript.EvaluatorException;

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

    protected final Log log = LogFactory.getLog(this.getClass());
    protected final ErrorReporter jsErrorReporter = new CommonsLogErrorReporter(this.log);

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

    @Override
    public int doAfterBody() throws JspException {
        final BodyContent bc = this.getBodyContent();

        // getJspWriter to output content
        final JspWriter out = bc.getEnclosingWriter();
        boolean scriptWritten = false;
        
        // if the portal is currently configured for aggregation, use 
        // YUICompressor to aggregate the javascript contained in the tag
        if (isCompressionEnabled()) {
            final Reader bodyReader = bc.getReader();
            try {
                final JavaScriptCompressor jsCompressor = new JavaScriptCompressor(bodyReader, this.jsErrorReporter);
                jsCompressor.compress(out,
                        this.lineBreakColumnNumber,
                        this.obfuscate,
                        false,
                        this.preserveAllSemiColons,
                        this.disableOptimizations);
                
                scriptWritten = true;
            }
            catch (EvaluatorException e) {
                log.warn("Failed to parse JS data to minify, falling back to non-minified JS.", e);
                bc.clearBody();
            }
            catch (IOException e) {
                log.warn("Failed to read or write JS data, falling back to non-minified JS.", e);
                bc.clearBody();
            }
        }
        
        //Handle both compression not working and compression being disabled
        if (!scriptWritten) {
            final Reader bodyReader = bc.getReader();
            try {
                IOUtils.copy(bodyReader, out);
            }
            catch (IOException e) {
                throw new JspException("Failed to write JS data to JSP", e);
            }
        }

        return SKIP_BODY;
    }
    
    protected boolean isCompressionEnabled() {
        //See if the ResourcesElementsProvider was provided as a request attribute, if so use the include type support provided there
        final ServletContext servletContext = this.pageContext.getServletContext();
        final ResourcesElementsProvider resourcesElementsProvider = ResourcesElementsProviderUtils.getOrCreateResourcesElementsProvider(servletContext);

        final HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
        final Included includedType = resourcesElementsProvider.getIncludedType(request);
        return Included.AGGREGATED.equals(includedType);
    }
}
