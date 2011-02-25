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

package org.jasig.resource.aggr;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.resource.aggr.om.BasicInclude;
import org.jasig.resource.aggr.om.Css;
import org.jasig.resource.aggr.om.Included;
import org.jasig.resource.aggr.om.Js;
import org.jasig.resource.aggr.om.ObjectFactory;
import org.jasig.resource.aggr.om.Resources;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ResourcesDaoImpl implements ResourcesDao {
    public static final String DEFAULT_ENCODING = "UTF-8";
    
    private static final ThreadLocal<Set<File>> VISITED = new ThreadLocal<Set<File>>() {
        @Override
        protected Set<File> initialValue() {
            return new LinkedHashSet<File>();
        }
    };
    
    private final CssResourceLoadCallback CSS_INSTANCE = new CssResourceLoadCallback();
    private final JsResourceLoadCallback JS_INSTANCE = new JsResourceLoadCallback();
    
    protected final Log logger;
    
    private Map<File, Resources> loadedResources = new ConcurrentHashMap<File, Resources>();
    private String schemaLocation;
    private final JAXBContext jaxbContext;
    private final String encoding;
    
    public ResourcesDaoImpl() {
        this(null, DEFAULT_ENCODING);
    }
    
    
    public ResourcesDaoImpl(String encoding) {
        this(null, encoding);
    }
    
    public ResourcesDaoImpl(Log logger, String encoding) {
        this.logger = logger != null ? logger : LogFactory.getLog(this.getClass());
        this.encoding = encoding;
        
        try {
            this.jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
        }
        catch (JAXBException e) {
            throw new RuntimeException("Failed to configure JAXBContext", e);
        }
    }
    
    /**
     * Map used to cache loaded resources, must be thread-safe
     */
    public void setLoadedResources(Map<File, Resources> loadedResources) {
        this.loadedResources = loadedResources;
    }

    @Override
    public void writeResources(final Resources resources, final File file) {
        try {
            Marshaller m = this.jaxbContext.createMarshaller();
            m.setProperty("jaxb.encoding", this.encoding);
            m.setProperty("jaxb.formatted.output", true);
            if (this.schemaLocation != null) {
                m.setProperty("jaxb.schemaLocation", this.schemaLocation);
            }
            m.marshal(resources, file);
        } catch (JAXBException e) {
            throw new AggregationException("Failed to marshal Resources to: " + file, e);
        }
    }

    @Override
    public Resources readResources(final File resourcesXml, Included scope) {
        final File filteredCacheKey = new File(resourcesXml, "__" + scope);
        Resources filteredResources = this.loadedResources.get(filteredCacheKey);
        if (filteredResources != null) {
            return filteredResources;
        }
        
        final Resources resources = this.readResources(resourcesXml);
        
        filteredResources = new Resources();

        //Copy over all parameters
        filteredResources.getParameter().addAll(resources.getParameter());
        
        //Copy matching CSS
        final List<Css> filteredCss = filteredResources.getCss();
        for (final Css css : resources.getCss()) {
            final Included included = css.getIncluded();
            if (included == Included.BOTH || scope == Included.BOTH || included == scope) {
                filteredCss.add(css);
            }
        }
        
        //Copy matching JS
        final List<Js> filteredJs = filteredResources.getJs();
        for (final Js js : resources.getJs()) {
            final Included included = js.getIncluded();
            if (included == Included.BOTH || scope == Included.BOTH || included == scope) {
                filteredJs.add(js);
            }
        }
        
        //Cache the filtered resources object
        this.loadedResources.put(filteredCacheKey, filteredResources);
        
        return filteredResources;
    }

    @Override
    public Resources readResources(final File resourcesXml) {
        Resources resources = this.loadedResources.get(resourcesXml);
        if (resources != null) {
            return resources;
        }
        
        final Set<File> visited = VISITED.get();
        try {
            if (!visited.add(resourcesXml)) {
                throw new IllegalArgumentException("There is a loop in the resource file imports: " + visited);
            }
            
            this.logger.debug("Loading Resources from: " + resourcesXml);
            
            try {
                final Unmarshaller u = this.jaxbContext.createUnmarshaller();
                resources = (Resources) u.unmarshal(resourcesXml);
            } catch (JAXBException e) {
                throw new AggregationException("Failed to unmarshal Resources file: " + resourcesXml, e);
            }
            
            final File resourcesParentDir = resourcesXml.getParentFile();
            
            //Iterate over CSS entries to handle imports
            final List<Css> cssList = resources.getCss();
            this.loadIncludes(resourcesParentDir, cssList, CSS_INSTANCE);
            
            //Iterate over Js entries to handle imports
            final List<Js> JsList = resources.getJs();
            this.loadIncludes(resourcesParentDir, JsList, JS_INSTANCE);
            
            this.loadedResources.put(resourcesXml, resources);
            return resources;
        }
        finally {
            visited.remove(resourcesXml);
            if (visited.size() == 0) {
                VISITED.remove();
            }
        }
    }
    
    @Override
    public String getAggregatedSkinName(String skinXmlName) {
        final String resourcesXmlBaseName = FilenameUtils.getBaseName(skinXmlName);
        final String resourcesXmlExtension = FilenameUtils.getExtension(skinXmlName);
        
        return resourcesXmlBaseName + ResourcesDao.AGGREGATED_SKIN_SUB_SUFFIX + resourcesXmlExtension;
    }
    
    @Override
    public boolean isAbsolute(BasicInclude include) {
        final String value = include.getValue();
        if (null == value) {
            return false;
        }
        return value.startsWith("/") || value.startsWith("http://") || value.startsWith("https://");
    }

    @Override
    public boolean isConditional(BasicInclude include) {
        return StringUtils.isNotBlank(include.getConditional());
    }

    protected <T extends BasicInclude> void loadIncludes(final File resourcesParentDir, final List<T> resources, final ResourceLoadCallback<T> callback) {
        for (final ListIterator<T> resourceItr = resources.listIterator(); resourceItr.hasNext();) {
            final T resource = resourceItr.next();
            if (resource.isImport()) {
                //Remove the import line
                resourceItr.remove();
                
                //Load the referenced Resources file
                final String importFile = resource.getValue();
                final File importedResourcesFile = new File(resourcesParentDir, importFile);
                final Resources importedResources = this.readResources(importedResourcesFile);
                
                //Insert all of the import entries at this location
                final List<T> resourceImports = callback.getImportedIncludes(importedResources);
                for (final T resourceImport : resourceImports) {
                    final T modifiedImport = callback.importResource(importFile, resourceImport);
                    resourceItr.add(modifiedImport);
                }
            }
        }
    }
    
    public interface ResourceLoadCallback<T extends BasicInclude> {
        public T importResource(String importBase, T source);
        public List<T> getImportedIncludes(Resources importedResources);
    }
    
    public class CssResourceLoadCallback implements ResourceLoadCallback<Css> {
        
        @Override
        public Css importResource(String importBase, Css source) {
            final Css modifiedCssImport = new Css();
            
            modifiedCssImport.setCompressed(source.isCompressed());
            modifiedCssImport.setConditional(source.getConditional());
            modifiedCssImport.setImport(source.isImport());
            modifiedCssImport.setIncluded(source.getIncluded());
            modifiedCssImport.setMedia(source.getMedia());
            
            if (isAbsolute(source)) {
                modifiedCssImport.setValue(source.getValue());
            }
            else {
                final String importPath = FilenameUtils.getPath(importBase);
                modifiedCssImport.setValue(importPath + source.getValue());
            }
            
            return modifiedCssImport;
        }

        @Override
        public List<Css> getImportedIncludes(Resources importedResources) {
            return importedResources.getCss();
        }
    }
    
    public class JsResourceLoadCallback implements ResourceLoadCallback<Js> {
        
        @Override
        public Js importResource(String importBase, Js source) {
            final Js modifiedJsImport = new Js();
            
            modifiedJsImport.setCompressed(source.isCompressed());
            modifiedJsImport.setConditional(source.getConditional());
            modifiedJsImport.setImport(source.isImport());
            modifiedJsImport.setIncluded(source.getIncluded());
            
            if (isAbsolute(source)) {
                modifiedJsImport.setValue(source.getValue());
            }
            else {
                final String importPath = FilenameUtils.getPath(importBase);
                modifiedJsImport.setValue(importPath + source.getValue());
            }
            
            return modifiedJsImport;
        }

        @Override
        public List<Js> getImportedIncludes(Resources importedResources) {
            return importedResources.getJs();
        }
    }
}
