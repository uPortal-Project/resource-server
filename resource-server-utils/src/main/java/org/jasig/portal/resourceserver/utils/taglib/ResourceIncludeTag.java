package org.jasig.portal.resourceserver.utils.taglib;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

public class ResourceIncludeTag extends TagSupport {

	private static final long serialVersionUID = 3381609818020633671L;

	private static final String RESOURCE_CONTEXT_INIT_PARAM = "resourceContextPath";
	private static final String DEFAULT_RESOURCE_CONTEXT = "/ResourceServingWebapp";

	protected String _var;
	protected String _url;
	protected String _resource;

	@Override
	public int doStartTag() throws JspException {

		try {
			_url = getRelativeUrlString(pageContext.getServletContext(),
					_resource);
		} catch (UnsupportedEncodingException e) {
			throw new JspException("UnsupportedEncodingException");
		}

		return EVAL_BODY_INCLUDE;

	}

	private String getRelativeUrlString(ServletContext servletContext,
			String resource) throws UnsupportedEncodingException {

		// attempt to get the servlet context of the resource serving webapp
		String resourceContext = servletContext
				.getInitParameter(RESOURCE_CONTEXT_INIT_PARAM);
		if (resourceContext == null) {
			// if no resource context path was defined in the web.xml, use the
			// default
			resourceContext = DEFAULT_RESOURCE_CONTEXT;
		} else if (!resourceContext.startsWith("/")) {
			// ensure that our context starts with a slash
			resourceContext = "/".concat(resourceContext);
		}

		// If the resource serving servlet context is available, create a URL
		// to the resource in that context.  If not, create a local URL for 
		// the requested resource.
		if (servletContext.getContext(resourceContext) == null) {
			resourceContext = servletContext.getContextPath();
		}

		// create a URL object from our url string
		return resourceContext.concat(resource);

	}

	@Override
	public int doEndTag() throws JspException {
		if (_var == null) {
			try {
				pageContext.getOut().print(_url);
			} catch (IOException e) {
				throw new JspTagException("Error: IOException while writing");
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
