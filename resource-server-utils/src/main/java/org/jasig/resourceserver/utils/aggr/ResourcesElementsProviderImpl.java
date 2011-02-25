package org.jasig.resource.aggr.util;

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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.resource.aggr.ResourcesDao;
import org.jasig.resource.aggr.om.Css;
import org.jasig.resource.aggr.om.Included;
import org.jasig.resource.aggr.om.Js;
import org.jasig.resource.aggr.om.Parameter;
import org.jasig.resource.aggr.om.Resources;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * {@link ResourcesDao} implementation that resolves the String argument using
 * the {@link ServletContext}.
 * 
 * Depends on {@link JAXBContext} to unmarshal the {@link Resources}.
 * 
 * @see ServletContextAware
 * @see JAXBContext
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class ResourcesElementsProviderImpl implements ResourceLoaderAware, ResourcesElementsProvider {
    public static final String AGGREGATED_THEME_PARAMETER = ResourcesElementsProviderImpl.class.getPackage().getName()
            + ".aggregated_theme";
    public static final String DEFAULT_AGGREGATION_ENABLED = Boolean.TRUE.toString();

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

    protected final Log logger = LogFactory.getLog(this.getClass());

    private final DocumentBuilder documentBuilder;
    private ResourceLoader resourceLoader;
    private ResourcesDao resourcesDao;
    
    public ResourcesElementsProviderImpl() {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            throw new IllegalStateException("Failed to create DocumentBuilder", e);
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void setResourcesDao(ResourcesDao resourcesDao) {
        this.resourcesDao = resourcesDao;
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
        if (Boolean.parseBoolean(System.getProperty(AGGREGATED_THEME_PARAMETER, DEFAULT_AGGREGATION_ENABLED))) {
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

    /* (non-Javadoc)
     * @see org.jasig.resource.aggr.util.ResourcesElementsProvider#getResourcesXmlFragment(javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    @Override
    public NodeList getResourcesXmlFragment(HttpServletRequest request, String skinXml) {
        final Resources skinResources = this.getResources(request, skinXml);
        if (skinResources == null) {
            logger.warn("Could not find skin file " + skinXml);
            return null;
        }

        final Document doc = this.documentBuilder.newDocument();
        final DocumentFragment headFragment = doc.createDocumentFragment();

        final String relativeRoot = request.getContextPath() + "/" + FilenameUtils.getPath(skinXml);
        for (final Css css : skinResources.getCss()) {
            appendCssNode(doc, headFragment, css, relativeRoot);
        }
        for (final Js js : skinResources.getJs()) {
            appendJsNode(doc, headFragment, js, relativeRoot);
        }

        return headFragment.getChildNodes();
    }
    
    protected Resources getResources(HttpServletRequest request, String skinXml) {
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
                final String aggregatedSkinXml = this.resourcesDao.getAggregatedSkinName(skinFile.getName());
                final File aggregatedSkinFile = new File(skinFile.getParentFile(), aggregatedSkinXml);
                if (aggregatedSkinFile.exists()) {
                    return this.resourcesDao.readResources(aggregatedSkinFile, includedType);
                }
                
                this.logger.warn("Could not find aggregated skin XML '" + aggregatedSkinFile + "' for '" + skinFile + "', falling back on unaggregated version.");
            }
            case BOTH:
            case PLAIN: {
                return this.resourcesDao.readResources(skinFile, includedType);
            }
            default: {
                throw new UnsupportedOperationException("Unkown Included type: " + includedType);
            }
        }
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
     * 
     * @param document
     * @param head
     * @param js
     * @param relativeRoot
     */
    protected void appendJsNode(Document document, DocumentFragment head, Js js, String relativeRoot) {
        String scriptPath = js.getValue();
        if (!this.resourcesDao.isAbsolute(js)) {
            scriptPath = FilenameUtils.normalize(relativeRoot + js.getValue());
            scriptPath = FilenameUtils.separatorsToUnix(scriptPath);
            if (logger.isDebugEnabled()) {
                logger.debug("translated relative js value " + js.getValue() + " to " + scriptPath);
            }
        }

        if (this.resourcesDao.isConditional(js)) {
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
            element.appendChild(document.createTextNode(""));

            head.appendChild(element);
        }
    }

    /**
     * Convert the {@link Css} argument to an HTML link tag and append it
     * to the {@link DocumentFragment}.
     * 
     * @param document
     * @param head
     * @param css
     * @param relativeRoot
     */
    protected void appendCssNode(Document document, DocumentFragment head, Css css, String relativeRoot) {
        String stylePath = css.getValue();
        if (!this.resourcesDao.isAbsolute(css)) {
            stylePath = FilenameUtils.normalize(relativeRoot + css.getValue());
            stylePath = FilenameUtils.separatorsToUnix(stylePath);
            if (logger.isDebugEnabled()) {
                logger.debug("translated relative css value " + css.getValue() + " to " + stylePath);
            }
        }

        if (this.resourcesDao.isConditional(css)) {
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

}
