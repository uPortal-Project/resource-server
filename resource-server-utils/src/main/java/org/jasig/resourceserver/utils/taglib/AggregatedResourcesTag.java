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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.jasig.resourceserver.utils.aggr.ResourcesElementsProvider;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProviderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @version $Revision$
 */
public class AggregatedResourcesTag extends TagSupport {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private static final long serialVersionUID = 3381609818020633671L;

    protected String _path;

    @Override
    public int doStartTag() throws JspException {
        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() throws JspException {
        final HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
        final ServletContext servletContext = this.pageContext.getServletContext();
        final ResourcesElementsProvider resourcesElementsProvider = ResourcesElementsProviderUtils.getOrCreateResourcesElementsProvider(request, servletContext);
        
        final String resourcesFragment = resourcesElementsProvider.getResourcesHtmlFragment(request, this._path);
        
        final JspWriter out = pageContext.getOut();
        try {
            out.print(resourcesFragment);
        }
        catch (IOException e) {
            throw new JspTagException("Error writing resources to page.", e);
        }

        return EVAL_PAGE;
    }

    public String getPath() {
        return _path;
    }
    
    public void setPath(String path) {
        this._path = path;
    }

}
