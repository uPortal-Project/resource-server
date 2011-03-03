package org.jasig.resourceserver.utils.aggr;

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

/**
 * 
 */

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.jasig.resourceserver.aggr.ResourcesDao;
import org.jasig.resourceserver.aggr.ResourcesDaoImpl;
import org.jasig.resourceserver.aggr.om.BasicInclude;
import org.jasig.resourceserver.aggr.om.Css;
import org.jasig.resourceserver.aggr.om.Included;
import org.jasig.resourceserver.aggr.om.Js;
import org.jasig.resourceserver.aggr.om.Parameter;
import org.jasig.resourceserver.aggr.om.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResourceLoader;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.MapMaker;

/**
 * {@link ResourcesDao} implementation that resolves the String argument using
 * the {@link ServletContext}.
 * 
 * @see ServletContextAware
 * @see JAXBContext
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class ResourcesElementsProviderImpl implements 
        ResourceLoaderAware, ServletContextAware, InitializingBean, 
        ResourcesElementsProvider {
    
    private static final String OPEN_COND_COMMENT_PRE = "[";
    private static final String OPEN_COND_COMMENT_POST = "]> ";
    private static final String CLOSE_COND_COMMENT = " <![endif]";
    private static final String OPEN_SCRIPT = "<script type=\"text/javascript\" src=\"";
    private static final String CLOSE_SCRIPT = "\"></script>";
    private static final String OPEN_STYLE = "<link rel=\"stylesheet\" type=\"text/css\" href=\"";
    private static final String CLOSE_STYLE = "\"/>";

    private static final String SCRIPT = "script";
    private static final String LINK = "link";
    private static final String REL = "rel";
    private static final String SRC = "src";
    private static final String HREF = "href";
    private static final String TYPE = "type";
    private static final String MEDIA = "media";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DocumentBuilder documentBuilder;
    private final TransformerFactory transformerFactory;
    private ResourceLoader resourceLoader;
    private ServletContext servletContext;
    private ResourcesDao resourcesDao;
    private boolean registerWithServletContext = true;
    private Map<String, String> resolvedResourceCache       = new MapMaker().maximumSize(500).makeMap();
    private Map<String, String> htmlResourcesCache          = new MapMaker().maximumSize(100).makeMap();
    private Map<String, DocumentFragment> xmlResourcesCache = new MapMaker().maximumSize(100).makeMap();
    
    public ResourcesElementsProviderImpl() {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            throw new IllegalStateException("Failed to create DocumentBuilder", e);
        }
        
        this.transformerFactory = TransformerFactory.newInstance();
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    /**
     * The DAO to use for accessing skin resources. If none is specified {@link ResourcesDaoImpl} is used
     */
    public void setResourcesDao(ResourcesDao resourcesDao) {
        this.resourcesDao = resourcesDao;
    }
    
    /**
     * Sets if this class should register itself in the {@link ServletContext} after initialization. Defaults to true.
     */
    public void setRegisterWithServletContext(boolean registerWithServletContext) {
        this.registerWithServletContext = registerWithServletContext;
    }
    
    /**
     * Thread-safe Map used to cache resolved resource URLs
     */
    public void setResolvedResourceCache(Map<String, String> resolvedResourceCache) {
        this.resolvedResourceCache = resolvedResourceCache;
    }

    /**
     * Thread safe Map used to cache generated skin resources HTML snippets
     */
    public void setHtmlResourcesCache(Map<String, String> htmlResourcesCache) {
        this.htmlResourcesCache = htmlResourcesCache;
    }

    /**
     * Thread safe Map used to cache generated skin resources XML snippets
     */
    public void setXmlResourcesCache(Map<String, DocumentFragment> xmlResourcesCache) {
        this.xmlResourcesCache = xmlResourcesCache;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.registerWithServletContext) {
            this.servletContext.setAttribute(RESOURCES_ELEMENTS_PROVIDER, this);
        }
        
        if (this.resourcesDao == null) {
            this.resourcesDao = new ResourcesDaoImpl();
        }
        
        if (this.resourceLoader == null) {
            this.resourceLoader = new ServletContextResourceLoader(this.servletContext);
        }
    }

    @Override
    public String resolveResourceUrl(HttpServletRequest request, String resource) {
        final Included includedType = this.getIncludedType(request);
        
        String resourceUrl;
        // If aggregation is enabled look in the cache to see if the resourceContext has already been determined
        if (Included.AGGREGATED == includedType) {
            resourceUrl = this.resolvedResourceCache.get(resource);
            if (resourceUrl != null) {
                return resourceUrl;
            }
        }
        
        final String resourceContextPath = this.resolveResourceContextPath(request, resource);
        
        //build the URL
        resourceUrl = resourceContextPath.concat(resource);
        
        this.logger.debug("Resoved {} to {}", resource, resourceUrl);
        
        // If aggregation is enabled cache the resolved resource
        if (Included.AGGREGATED == includedType) {
            this.resolvedResourceCache.put(resource, resourceUrl);
        }

        return resourceUrl;
    }
    
    @Override
    public void setDefaultIncludedType(Included included) {
        switch (included) {
            case AGGREGATED: {
                System.setProperty(AGGREGATED_THEME_PARAMETER, Boolean.TRUE.toString());
                break;
            }
            case PLAIN: {
                System.setProperty(AGGREGATED_THEME_PARAMETER, Boolean.FALSE.toString());
                this.resolvedResourceCache.clear();
                this.htmlResourcesCache.clear();
                this.xmlResourcesCache.clear();
                break;
            }
            case BOTH:
            default: {
                throw new UnsupportedOperationException("Unsupported Included type: " + included);
            }
        }
    }
    

    @Override
    public Included getDefaultIncludedType() {
        String aggregationParameter = System.getProperty(AGGREGATED_THEME_PARAMETER);
        if (aggregationParameter == null) {
            aggregationParameter = System.getProperty(LEGACY_AGGREGATED_THEME_PARAMETER_1);
        }
        if (aggregationParameter == null) {
            aggregationParameter = System.getProperty(LEGACY_AGGREGATED_THEME_PARAMETER_2);
        }
        if (aggregationParameter == null) {
            aggregationParameter = DEFAULT_AGGREGATION_ENABLED;
        }
        if (Boolean.parseBoolean(aggregationParameter)) {
            return Included.AGGREGATED;
        }

        return Included.PLAIN;
    }

    /* (non-Javadoc)
     * @see org.jasig.resource.aggr.util.ResourcesElementsProvider#getIncludedType(javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    @Override
    public Included getIncludedType(HttpServletRequest request) {
        return this.getDefaultIncludedType();
    }
    
    /* (non-Javadoc)
     * @see org.jasig.resource.aggr.util.ResourcesElementsProvider#getResourcesParameter(javax.servlet.http.HttpServletRequest, java.lang.String, java.lang.String)
     */
    @Override
    public String getResourcesParameter(HttpServletRequest request, String skinXml, String name) {
        final Resources skinResources = this.getResources(request, skinXml);
        if (skinResources == null) {
            logger.warn("Could not find skin file " + skinXml);
            return null;
        }
        
        for (final Parameter parameter : skinResources.getParameter()) {
            if (parameter.getName().equals(name)) {
                return parameter.getValue();
            }
        }
        
        return null;
    }

    @Override
    public NodeList getResourcesXmlFragment(HttpServletRequest request, String skinXml) {
        final DocumentFragment headFragment = getResourcesXml(request, skinXml);

        return headFragment.getChildNodes();
    }
    
    @Override
    public String getResourcesHtmlFragment(HttpServletRequest request, String skinXml) {
        final Included includedType = this.getIncludedType(request);
        
        String htmlFragment;
        if (Included.AGGREGATED == includedType) {
            htmlFragment = this.htmlResourcesCache.get(skinXml);
            if (htmlFragment != null) {
                return htmlFragment;
            }
        }
        
        final DocumentFragment resourcesXml = this.getResourcesXml(request, skinXml);

        final StringWriter stringWriter = new StringWriter();
        try {
            final Transformer transformer = transformerFactory.newTransformer();
            if (Included.PLAIN == this.getIncludedType(request)) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            }
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(resourcesXml), new StreamResult(stringWriter));
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to serialize XML fragment to HTML fragment", e);
        }
        
        htmlFragment = stringWriter.toString();
        if (Included.AGGREGATED == includedType) {
            this.htmlResourcesCache.put(skinXml, htmlFragment);
        }
        return htmlFragment;
    }

    @Override
    public Resources getResources(HttpServletRequest request, String skinXml) {
        final Included includedType = this.getIncludedType(request);

        final Resource skinResource = getResource(skinXml);
        final File skinFile;
        try {
            skinFile = skinResource.getFile();
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Failed to get File for skin XML path: " + skinXml, e);
        }

        switch (includedType) {
            case AGGREGATED: {
                final String aggregatedSkinXml = resourcesDao.getAggregatedSkinName(skinFile.getName());
                final File aggregatedSkinFile = new File(skinFile.getParentFile(), aggregatedSkinXml);
                if (aggregatedSkinFile.exists()) {
                    return resourcesDao.readResources(aggregatedSkinFile, includedType);
                }
                
                this.logger.warn("Could not find aggregated skin XML '" + aggregatedSkinFile + "' for '" + skinFile + "', falling back on unaggregated version.");
            }
            case PLAIN: {
                return resourcesDao.readResources(skinFile, includedType);
            }
            default: {
                throw new UnsupportedOperationException("Unkown Included type: " + includedType);
            }
        }
    }

    /**
     * If the resource serving servlet context is available and the resource
     * is available in the context, create a URL to the resource in that context.
     * If not, create a local URL for the requested resource.
     */
    protected String resolveResourceContextPath(HttpServletRequest request, String resource) {
        final String resourceContextPath = this.getResourceServerContextPath();
        
        this.logger.debug("Attempting to locate resource serving webapp with context path: {}", resourceContextPath);
        
        //Try to resolve the 
        final ServletContext resourceContext = this.servletContext.getContext(resourceContextPath);
        if (resourceContext == null || !resourceContextPath.equals(resourceContext.getContextPath())) {
            this.logger.warn("Could not find resource serving webapp under context path {} ensure the resource server is deployed and cross context dispatching is enable for this web application", resourceContextPath);
            return request.getContextPath();
        }
        
        this.logger.debug("Found resource serving webapp at: {}", resourceContextPath);
        
        URL url = null;
        try {
            url = resourceContext.getResource(resource);
        }
        catch (MalformedURLException e) {
            //Ignore
        }
        
        if (url == null) {
            this.logger.debug("Resource serving webapp {} doesn't contain resource {} Falling back to the local resource.", resourceContextPath, resource);
            return request.getContextPath();
        }
        
        this.logger.debug("Resource serving webapp {} contains resource {} Using resource server.", resourceContextPath, resource);
        return resourceContextPath;
    }

    /**
     * Determine the context name of the resource serving webapp 
     */
    protected String getResourceServerContextPath() {
        final String resourceContextPath = this.servletContext.getInitParameter(RESOURCE_CONTEXT_INIT_PARAM);
        if (resourceContextPath == null) {
            // if no resource context path was defined in the web.xml, use the
            // default
            return DEFAULT_RESOURCE_CONTEXT;
        } 

        if (!resourceContextPath.startsWith("/")) {
            // ensure that our context starts with a slash
            return "/".concat(resourceContextPath);
        }
        
        return resourceContextPath;
    }

    /**
     * Build XML {@link DocumentFragment} of link and script tags for the specified skin file 
     */
    protected DocumentFragment getResourcesXml(HttpServletRequest request, String skinXml) {
        final Included includedType = this.getIncludedType(request);
        
        DocumentFragment headFragment;
        if (Included.AGGREGATED == includedType) {
            headFragment = this.xmlResourcesCache.get(skinXml);
            if (headFragment != null) {
                return headFragment;
            }
        }
        
        final Resources skinResources = this.getResources(request, skinXml);
        if (skinResources == null) {
            logger.warn("Could not find skin file " + skinXml);
            return null;
        }

        final Document doc = this.documentBuilder.newDocument();
        headFragment = doc.createDocumentFragment();

        final String relativeRoot = request.getContextPath() + "/" + FilenameUtils.getPath(skinXml);
        for (final Css css : skinResources.getCss()) {
            appendCssNode(request, doc, headFragment, css, relativeRoot);
        }
        for (final Js js : skinResources.getJs()) {
            appendJsNode(request, doc, headFragment, js, relativeRoot);
        }
        
        if (Included.AGGREGATED == includedType) {
            this.xmlResourcesCache.put(skinXml, headFragment);
        }
        return headFragment;
    }

    protected Resource getResource(String skinXml) {
        if (!skinXml.startsWith("/")) {
            skinXml = "/" + skinXml;
        }

        return this.resourceLoader.getResource(skinXml);
    }

    /**
     * Convert the {@link Js} argument to an HTML script tag and append it
     * to the {@link DocumentFragment}.
     */
    protected void appendJsNode(HttpServletRequest request, Document document, DocumentFragment head, Js js, String relativeRoot) {
        final String scriptPath = getElementPath(request, js, relativeRoot);

        if (resourcesDao.isConditional(js)) {
            Comment c = document.createComment("");
            c.appendData(OPEN_COND_COMMENT_PRE);
            c.appendData(js.getConditional());
            c.appendData(OPEN_COND_COMMENT_POST);
            c.appendData(OPEN_SCRIPT);
            c.appendData(scriptPath);
            c.appendData(CLOSE_SCRIPT);
            c.appendData(CLOSE_COND_COMMENT);
            head.appendChild(c);
        }
        else {
            Element element = document.createElement(SCRIPT);
            element.setAttribute(TYPE, "text/javascript");
            element.setAttribute(SRC, scriptPath);
            element.appendChild(document.createTextNode(" "));

            head.appendChild(element);
        }
    }

    /**
     * Convert the {@link Css} argument to an HTML link tag and append it
     * to the {@link DocumentFragment}.
     */
    protected void appendCssNode(HttpServletRequest request, Document document, DocumentFragment head, Css css, String relativeRoot) {
        final String stylePath = getElementPath(request, css, relativeRoot);

        if (resourcesDao.isConditional(css)) {
            Comment c = document.createComment("");
            c.appendData(OPEN_COND_COMMENT_PRE);
            c.appendData(css.getConditional());
            c.appendData(OPEN_COND_COMMENT_POST);
            c.appendData(OPEN_STYLE);
            c.appendData(stylePath);
            c.appendData("\" media=\"");
            c.appendData(css.getMedia());
            c.appendData(CLOSE_STYLE);
            c.appendData(CLOSE_COND_COMMENT);
            head.appendChild(c);
        }
        else {
            Element element = document.createElement(LINK);
            element.setAttribute(REL, "stylesheet");
            element.setAttribute(TYPE, "text/css");
            element.setAttribute(HREF, stylePath);
            element.setAttribute(MEDIA, css.getMedia());
            head.appendChild(element);
        }
    }

    protected <T extends BasicInclude> String getElementPath(HttpServletRequest request, T basicInclude, String relativeRoot) {
        String path = basicInclude.getValue();
        
        if (!resourcesDao.isAbsolute(basicInclude)) {
            path = FilenameUtils.normalize(relativeRoot + path);
            path = FilenameUtils.separatorsToUnix(path);
            if (logger.isDebugEnabled()) {
                logger.debug("translated relative path {} to {}", basicInclude.getValue(), path);
            }
        }
        else if (basicInclude.isResource()) {
            path = this.resolveResourceUrl(request, path);
        }
        
        return path;
    }
}
