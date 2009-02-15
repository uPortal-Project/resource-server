package org.jasig.resourceserver.utils.filter;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CacheExpirationFilter sets a far-future expiration timeout on the HTTP 
 * response of the filtered resource.  This filter is intended for resources
 * which will not be changed, such as versioned javascript and css files.  Any
 * resources protected by this filter should be renamed upon update.
 * 
 * init-params:<br/>
 *  cacheMaxAge - Sets the max age in seconds to be used in the cache headers for cached resources,
 *      defaults to 1 year (31536000).<br/>
 *      
 *  regenerateHeadersInterval - The interval in milliseconds to regenerate the cache headers, setting
 *      below 1 results in the headers being generated for every request and will cause performance
 *      problems. Defaults to 1 minute (60000).
 * 
 * @author Jen Bourey
 */
public class CacheExpirationFilter implements Filter {
    public static final String MAX_AGE_INIT_PARAM = "cacheMaxAge";
    public static final String REGENERATE_TIME_INIT_PARAM = "regenerateHeadersInterval";
    
    private static final int YEAR_OF_SECONDS = 365 * 24 * 60 * 60;
        
    protected final Log logger = LogFactory.getLog(this.getClass());
    private final DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z");
    
    private String cachedControlString;
    private String cachedExpiresString;
    private long lastGenerated;
    
    //Default resource cache time is 1 year
    private int maxAge = YEAR_OF_SECONDS;
    
    //Default header cache time is 60 seconds
    private long cacheTime = 60 * 1000;
    
    
	/**
	 * {@inheritDoc}
	 */
	public void init(FilterConfig config) throws ServletException {
	    final String maxAgeStr = config.getInitParameter(MAX_AGE_INIT_PARAM);
	    if (maxAgeStr != null) {
    	    try {
    	        this.maxAge = Integer.valueOf(maxAgeStr);
    	    }
    	    catch (NumberFormatException nfe) {
    	        throw new IllegalArgumentException("Specified initParamter '" + MAX_AGE_INIT_PARAM + "' is not a number. (" + maxAgeStr + ")", nfe);
    	    }
	    }
	    
	    if (this.maxAge < 1) {
	        throw new IllegalArgumentException("Specified initParamter '" + MAX_AGE_INIT_PARAM + "' must be greater than 0, (" + this.maxAge + ")");
	    }

	    if (this.maxAge < YEAR_OF_SECONDS ) {
	        this.logger.warn("Cache maxAge is set to " + this.maxAge + " which is below the recommended minimum setting of " + YEAR_OF_SECONDS);
	    }
	    
	    final String cacheTimeStr = config.getInitParameter(REGENERATE_TIME_INIT_PARAM);
	    if (cacheTimeStr != null) {
	        try {
	            this.cacheTime = Long.valueOf(cacheTimeStr);
	        }
	        catch (NumberFormatException nfe) {
	            throw new IllegalArgumentException("Specified initParamter '" + REGENERATE_TIME_INIT_PARAM + "' is not a number. (" + cacheTimeStr + ")", nfe);
	        }
	    }

        if (this.cacheTime <= 0) {
            this.logger.warn(REGENERATE_TIME_INIT_PARAM + " is <= 0, caching of headers will be disabled. THIS WILL CAUSE PERFORMANCE PROBLEMS AND SHOULD BE RESOLVED FOR PRODUCTION USE.");
        }
	    
	    this.cachedControlString = "public, max-age=" + this.maxAge;
	    this.getExpiresHeader();
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

	    // add the cache expiration time to the response
        if (response instanceof HttpServletResponse && request instanceof HttpServletRequest) {
			final HttpServletResponse httpResponse = (HttpServletResponse) response;

			final String expires = this.getExpiresHeader();
			httpResponse.setHeader("Expires", expires);
			
			httpResponse.setHeader("Cache-Control", this.cachedControlString);
		}
		
		// continue
		chain.doFilter(request, response);
	}

	protected String getExpiresHeader() {
	    if (this.cacheTime <= 0 || this.cachedExpiresString == null || System.currentTimeMillis() < (this.lastGenerated + this.cacheTime)) {
	        //sync inside if could result in multiple threads updating the variable sequentially
	        // but that case doesn't break anything, it just uses a little more CPU
	        synchronized (this.dateFormat) {
	            final Calendar cal = Calendar.getInstance();
	            cal.add(Calendar.SECOND, this.maxAge);
	            
	            this.cachedExpiresString = this.dateFormat.format(cal.getTime());
	            this.lastGenerated = System.currentTimeMillis();
            }
	    }
	    
	    return this.cachedExpiresString;
	}
}
