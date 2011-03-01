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

/**
 * Determines if the resource serving webapp is deployed via a cross-context lookup using
 * {@link ServletContext#getContext(String)}. If it is the resource service webapp context
 * path is used as the base of the resource URL, if it is not the current webapp's context
 * path is used. The default context path for the resource serving webapp is defined by 
 * {@link ResourcesElementsProvider#DEFAULT_RESOURCE_CONTEXT}.
 * 
 * init-params:<br/>
 *  resourceContextPath - Overrides the default servlet context path for the resource serving webapp.
 * 
 * @version $Revision$
 */
public class ResourceIncludeTag extends TagSupport {
    private static final long serialVersionUID = 3381609818020633671L;

    protected String _var;
    protected String _url;
    protected String _resource;

    @Override
    public int doStartTag() throws JspException {
        final ServletContext servletContext = pageContext.getServletContext();
        final ResourcesElementsProvider resourcesElementsProvider = ResourcesElementsProviderUtils.getOrCreateResourcesElementsProvider(servletContext);
        
        final HttpServletRequest httpServletRequet = (HttpServletRequest)pageContext.getRequest();
        _url = resourcesElementsProvider.resolveResourceUrl(httpServletRequet, _resource);

        return EVAL_BODY_INCLUDE;

    }

    @Override
    public int doEndTag() throws JspException {
        if (_var == null) {
            final JspWriter out = pageContext.getOut();
            try {
                out.print(_url);
            } catch (IOException e) {
                throw new JspTagException("Error writing URL to page.", e);
            }
        } else {
            pageContext.setAttribute(_var, _url);
        }
        return EVAL_PAGE;
    }

    public String getVar() {
        return _var;
    }

    public void setVar(String var) {
        this._var = var;
    }

    public String getValue() {
        return _resource;
    }

    public void setValue(String resource) {
        // make sure our resource path starts with a leading forward slash
        if (!resource.startsWith("/")) {
            this._resource = "/".concat(resource);
        } else {
            this._resource = resource;
        }
    }

    public String getUrl() {
        return _url;
    }

}
