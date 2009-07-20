package org.jasig.resourceserver.utils.taglib;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Determines if the resource serving webapp is deployed via a cross-context lookup using
 * {@link ServletContext#getContext(String)}. If it is the resource service webapp context
 * path is used as the base of the resource URL, if it is not the current webapp's context
 * path is used. The default context path for the resource serving webapp is defined by 
 * {@link #DEFAULT_RESOURCE_CONTEXT}.
 * 
 * init-params:<br/>
 *  resourceContextPath - Overrides the default servlet context path for the resource serving webapp.
 * 
 * @version $Revision$
 */
public class ResourceIncludeTag extends TagSupport {
    private static final long serialVersionUID = 3381609818020633671L;

    public static final String RESOURCE_CONTEXT_INIT_PARAM = "resourceContextPath";
    public static final String DEFAULT_RESOURCE_CONTEXT = "/ResourceServingWebapp";
    
    private static final String RESOURCE_URL_ATTR_BASE = ResourceIncludeTag.class.getName() + ".resourceUrlBase.";

    protected String _var;
    protected String _url;
    protected String _resource;

    @Override
    public int doStartTag() throws JspException {
        final HttpServletRequest httpServletRequet = (HttpServletRequest)pageContext.getRequest();
        final ServletContext servletContext = pageContext.getServletContext();
        _url = getRelativeUrlString(httpServletRequet, servletContext, _resource);

        return EVAL_BODY_INCLUDE;

    }

    private String getRelativeUrlString(HttpServletRequest httpServletRequest, ServletContext servletContext, String resource) {
        final String resourceAttributeName = RESOURCE_URL_ATTR_BASE + resource;
        
        // Look in the request to see if the resourceContext has already been determined
        String resourceUrl = (String)httpServletRequest.getAttribute(resourceAttributeName);
        if (resourceUrl != null) {
            return resourceUrl;
        }
        
        //Look in the session for the resolved resource URL or resource context
        final HttpSession session = httpServletRequest.getSession(false);
        if (session != null) {
            resourceUrl = (String)session.getAttribute(resourceAttributeName);
            if (resourceUrl != null) {
                return resourceUrl;
            }
        }
        
        // attempt to get the servlet context of the resource serving webapp
        String resourceContextPath = servletContext.getInitParameter(RESOURCE_CONTEXT_INIT_PARAM);
        if (resourceContextPath == null) {
            // if no resource context path was defined in the web.xml, use the
            // default
            resourceContextPath = DEFAULT_RESOURCE_CONTEXT;
        } else if (!resourceContextPath.startsWith("/")) {
            // ensure that our context starts with a slash
            resourceContextPath = "/".concat(resourceContextPath);
        }
        
        // If the resource serving servlet context is available and the resource
        // is available in the context, create a URL to the resource in that context.
        // If not, create a local URL for the requested resource.
        final ServletContext resourceContext = servletContext.getContext(resourceContextPath);
        if (resourceContext != null) {
            if (resourceContext.getRequestDispatcher(resource) == null) {
                resourceContextPath = httpServletRequest.getContextPath();
            }
        }
        else {
            resourceContextPath = httpServletRequest.getContextPath();
        }

        
        //build the URL
        resourceUrl = resourceContextPath.concat(resource);
        
        // Cache the resourceContext as a request and session attribute
        httpServletRequest.setAttribute(resourceAttributeName, resourceContextPath);
        if (session != null) {
            session.setAttribute(resourceAttributeName, resourceUrl);
        }

        return resourceUrl;

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
