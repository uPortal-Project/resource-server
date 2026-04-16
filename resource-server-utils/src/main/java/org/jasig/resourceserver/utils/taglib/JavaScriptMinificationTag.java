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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.io.IOUtils;

/**
 * JSP tag that previously minified inline JavaScript using YUI Compressor.
 * Minification is now handled at build time by esbuild; this tag passes
 * content through unchanged and is retained for backward compatibility.
 *
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @deprecated In-page JS minification via this tag is no longer performed.
 *             Use build-time minification instead.
 */
@Deprecated
public class JavaScriptMinificationTag extends BodyTagSupport {

    private static final long serialVersionUID = 1950546842057709745L;

    @Override
    public int doAfterBody() throws JspException {
        final BodyContent bc = this.getBodyContent();
        final JspWriter out = bc.getEnclosingWriter();
        try {
            IOUtils.copy(bc.getReader(), out);
        } catch (IOException e) {
            throw new JspException("Failed to write JS data to JSP", e);
        }
        return SKIP_BODY;
    }
}
