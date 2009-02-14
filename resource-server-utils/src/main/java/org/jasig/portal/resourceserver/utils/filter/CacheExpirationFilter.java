package org.jasig.portal.resourceserver.utils.filter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CacheExpirationFilter sets a far-future expiration timeout on the HTTP 
 * response of the filtered resource.  This filter is intended for resources
 * which will not be changed, such as versioned javascript and css files.  Any
 * resources protected by this filter should be renamed upon update.
 * 
 * @author Jen Bourey
 */
public class CacheExpirationFilter implements Filter {

	/**
	 * {@inheritDoc}
	 */
	public void init(FilterConfig config) throws ServletException {
		// no initialization tasks
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroy() {
		// no destroy tasks
	}

	/**
	 * {@inheritDoc}
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		// get the expires date
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 10);
		DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z");
		String expires = df.format(cal.getTime());
		
		// add the cache expiration time to the response
		if (response instanceof HttpServletResponse && request instanceof HttpServletRequest) {
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.setHeader("Expires", expires);
		}
		
		// continue
		chain.doFilter(request, response);
	}

}
