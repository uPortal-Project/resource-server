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
package org.jasig.resourceserver.utils.taglib;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backing class for the <code>&lt;rs:compressJs&gt;</code> JSP tag.
 *
 * <p><strong>@deprecated since 1.5.1.</strong> This tag no longer minifies its
 * body content and simply passes the content through unchanged.</p>
 *
 * <p>YUI Compressor was removed in 1.5.1 in favor of build-time esbuild
 * minification of files under <code>/rs/**</code>. That build-time minification
 * does NOT apply to inline JSP script content, so preserving the previous
 * behavior would require forking esbuild per tag invocation at request time,
 * which has been judged too costly compared to the benefit.</p>
 *
 * <p>Portlet maintainers should remove usages of this tag. The tag is retained
 * for binary/source compatibility in 1.5.x and will be removed entirely in a
 * future major release.</p>
 *
 * <p>Attributes on the tag (<code>lineBreakColumnNumber</code>,
 * <code>obfuscate</code>, <code>preserveAllSemiColons</code>,
 * <code>disableOptimizations</code>) are retained for backward compatibility
 * but have no effect.</p>
 *
 * @author Jen Bourey, jbourey@unicon.net
 * @deprecated Use build-time minification via esbuild for files under
 *             <code>/rs/**</code>. Inline JSP scripts are no longer minified.
 */
@Deprecated
public class JavaScriptMinificationTag extends BodyTagSupport {

    private static final long serialVersionUID = 1950546842057709745L;

    private static final Log log = LogFactory.getLog(JavaScriptMinificationTag.class);

    /** Used to log the deprecation warning exactly once per JVM. */
    private static final AtomicBoolean DEPRECATION_LOGGED = new AtomicBoolean(false);

    private int lineBreakColumnNumber = 10000;
    private boolean obfuscate = true;
    private boolean preserveAllSemiColons = true;
    private boolean disableOptimizations = false;

    /** @deprecated Has no effect since 1.5.1. */
    @Deprecated
    public void setLineBreakColumnNumber(int lineBreakColumnNumber) {
        this.lineBreakColumnNumber = lineBreakColumnNumber;
    }

    /** @deprecated Has no effect since 1.5.1. */
    @Deprecated
    public void setObfuscate(boolean obfuscate) {
        this.obfuscate = obfuscate;
    }

    /** @deprecated Has no effect since 1.5.1. */
    @Deprecated
    public void setPreserveAllSemiColons(boolean preserveAllSemiColons) {
        this.preserveAllSemiColons = preserveAllSemiColons;
    }

    /** @deprecated Has no effect since 1.5.1. */
    @Deprecated
    public void setDisableOptimizations(boolean disableOptimizations) {
        this.disableOptimizations = disableOptimizations;
    }

    @Override
    public int doAfterBody() throws JspException {
        if (DEPRECATION_LOGGED.compareAndSet(false, true)) {
            log.warn("<rs:compressJs> is deprecated since resource-server 1.5.1 and"
                    + " no longer minifies inline JSP script content. The tag now"
                    + " passes content through unchanged. Remove usages; the tag"
                    + " will be removed in a future major release.");
        }

        final BodyContent bc = this.getBodyContent();
        final JspWriter out = bc.getEnclosingWriter();
        final Reader bodyReader = bc.getReader();
        try {
            IOUtils.copy(bodyReader, out);
        } catch (IOException e) {
            throw new JspException("Failed to write JS data to JSP", e);
        }

        return SKIP_BODY;
    }
}
