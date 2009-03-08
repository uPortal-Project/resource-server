package org.jasig.resourceserver.utils.filter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.filter.GenericFilterBean;

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
 *  regenerateHeadersInterval - The interval in milliseconds to regenerate the cache headers,
 *      defaults to 1 second (1000).
 * 
 * @author Jen Bourey
 */
public class CacheExpirationFilter extends GenericFilterBean {
    private static final int YEAR_OF_SECONDS = 365 * 24 * 60 * 60;
        
    protected final Log logger = LogFactory.getLog(this.getClass());
    private final DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z");
    
    private Timer headerUpdateTimer;
    private String cachedControlString;
    private String cachedExpiresString;
    
    //Default resource cache time is 1 year
    private int cacheMaxAge = YEAR_OF_SECONDS;
    
    //Default header cache time is 1 second
    private long regenerateHeadersInterval = 1000;
    

    public int getCacheMaxAge() {
        return this.cacheMaxAge;
    }
    /**
     * @param cacheMaxAge Sets the max age in seconds to be used in the cache headers for cached resources, defaults to 1 year (31536000)
     */
    public void setCacheMaxAge(int cacheMaxAge) {
        if (cacheMaxAge < 1) {
            throw new IllegalArgumentException("Specified initParamter 'cacheMaxAge' must be greater than 0, (" + cacheMaxAge + ")");
        }

        if (cacheMaxAge < YEAR_OF_SECONDS ) {
            this.logger.warn("Cache cacheMaxAge is set to " + cacheMaxAge + " which is below the recommended minimum setting of " + YEAR_OF_SECONDS);
        }
        
        this.cacheMaxAge = cacheMaxAge;
    }

    public long getRegenerateHeadersInterval() {
        return this.regenerateHeadersInterval;
    }
    /**
     * @param regenerateHeadersInterval The interval in milliseconds to regenerate the cache headers, defaults to 1 second (1000).
     */
    public void setRegenerateHeadersInterval(long regenerateHeadersInterval) {
        if (regenerateHeadersInterval < 1) {
            throw new IllegalArgumentException("'regenerateHeadersInterval' must be greater than 0, (" + regenerateHeadersInterval + ")");
        }
        
        this.regenerateHeadersInterval = regenerateHeadersInterval;
    }
    
    
    
    /* (non-Javadoc)
     * @see org.springframework.web.filter.GenericFilterBean#initFilterBean()
     */
    @Override
    protected void initFilterBean() throws ServletException {
        //Generate cache control value
        this.cachedControlString = "public, max-age=" + this.cacheMaxAge;
        
        //Initialize cache header
        this.updateCacheHeader();
        
        //Start timer to periodically refresh the cache header
        this.headerUpdateTimer = new Timer("CacheHeaderUpdateTimer", true);
        this.headerUpdateTimer.schedule(new CacheHeaderUpdater(), this.regenerateHeadersInterval, this.regenerateHeadersInterval);
    }
    
    /**
	 * {@inheritDoc}
	 */
    @Override
	public void destroy() {
	    this.headerUpdateTimer.cancel();
	    this.headerUpdateTimer = null;
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
	    return this.cachedExpiresString;
	}

    protected void updateCacheHeader() {
        synchronized (this.dateFormat) {
            final Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, this.cacheMaxAge);
            this.cachedExpiresString = this.dateFormat.format(cal.getTime());
        }
    }
    
    /**
     * Simple task that calls {@link CacheExpirationFilter#updateCacheHeader()}
     */
    private final class CacheHeaderUpdater extends TimerTask {
        /* (non-Javadoc)
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run() {
            updateCacheHeader();
        }
    }
}
