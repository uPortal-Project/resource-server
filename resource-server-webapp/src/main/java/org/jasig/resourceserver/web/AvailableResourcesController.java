/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.resourceserver.web;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AvailableResourcesController extends AbstractController {

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final Map<String, Object> model = new HashMap<String, Object>();
        
        final ApplicationContext applicationContext = this.getApplicationContext();
        
        final Resource[] jsResources = applicationContext.getResources("**/*.js");
        final SortedSet<String> jsContextResources = this.getContextResources(jsResources);
        model.put("jsResources", jsContextResources);
        
        final Resource[] cssResources = applicationContext.getResources("**/*.css");
        final SortedSet<String> cssContextResources = this.getContextResources(cssResources);
        model.put("cssResources", cssContextResources);
        
        return new ModelAndView("availableResources", model);
    }

    protected SortedSet<String> getContextResources(final Resource[] resources) {
        final SortedSet<String> jsContextResources = new TreeSet<String>();
        for (final Resource resource : resources) {
            if (resource instanceof ContextResource) {
                final ContextResource contextResource = (ContextResource)resource;
                jsContextResources.add(contextResource.getPathWithinContext());
            }
        }
        return jsContextResources;
    }

}
