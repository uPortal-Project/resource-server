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
package org.jasig.resourceserver.utils.filter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", new Locale("en"));
    
    private Timer headerUpdateTimer;
    private String cachedControlString;
    private String cachedExpiresString;
    
    //Default resource cache time is 1 year
    private int cacheMaxAge = YEAR_OF_SECONDS;
    
    //Default header cache time is 1 second
    private long regenerateHeadersInterval = 1000;
    
    public CacheExpirationFilter() {
        final TimeZone timeZone = TimeZone.getTimeZone("GMT");
        this.dateFormat.setTimeZone(timeZone);
    }
    

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
        final ServletContext servletContext = this.getServletContext();
        final String servletContextPath = servletContext.getContextPath();
        this.headerUpdateTimer = new Timer(servletContextPath + "-CacheHeaderUpdateTimer", true);
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
