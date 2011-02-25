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
package org.jasig.resource.aggr;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jasig.resourceserver.aggr.ResourcesDaoImpl;
import org.jasig.resourceserver.aggr.om.Css;
import org.jasig.resourceserver.aggr.om.Included;
import org.jasig.resourceserver.aggr.om.Js;
import org.jasig.resourceserver.aggr.om.Resources;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * Test harness for JAXB marshalling/unmarshalling of {@link Resources}
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class JAXBTest {
    private static final String ENCODING = "UTF-8";

    @BeforeClass
    public static void xmlUnitInit() {
        XMLUnit.setIgnoreWhitespace(true);
    }
    
    private ResourcesDaoImpl resourcesDao;
    
    @Before
    public void setup() throws Exception {
        this.resourcesDao = new ResourcesDaoImpl(ENCODING);
    }
    
    @After
    public void teardown() {
        this.resourcesDao = null;
    }

    /**
     * Marshal an empty skinConfiguration to System.out, test passes if no exception thrown.
     */
    @Test
    public void testMarshalEmptyConfiguration() throws Exception {
        Resources config = new Resources();
        
        final File tempConfig = File.createTempFile("tempConfig.", ".xml");
        tempConfig.deleteOnExit();
        this.resourcesDao.writeResources(config, tempConfig);
        
        final String expected = IOUtils.toString(this.getClass().getResourceAsStream("/JAXBTest/emptyConfiguration.xml"));
        final String actual = FileUtils.readFileToString(tempConfig, ENCODING);
        
        final Diff d = new Diff(expected, actual);
        assertTrue(d.toString(), d.similar());
    }
    
    /**
     * Unmarshal an empty skinConfiguration.
     * 
     * @throws Exception
     */
    @Test
    public void testUnmarshalEmptyConfiguration() throws Exception {
        final File resourceFile = new ClassPathResource("JAXBTest/emptyConfiguration.xml").getFile();
        
        Resources config = this.resourcesDao.readResources(resourceFile);
        Assert.assertNotNull(config);
        Assert.assertNotNull(config.getJs());
        Assert.assertNotNull(config.getCss());
        Assert.assertEquals(0, config.getJs().size());
        Assert.assertEquals(0, config.getCss().size());
    }
    
    /**
     * Marshal an basic skinConfiguration to System.out, test passes if no exception thrown.
     */
    @Test
    public void testMarshalControl() throws Exception {
        Resources config = new Resources();
        Js js = new Js();
        js.setValue("/path/to/some/javascript.js");
        config.getJs().add(js);
        Css css = new Css();
        css.setValue("/path/to/some/stylesheet.css");
        css.setMedia("screen");
        config.getCss().add(css);
        
        final File tempConfig = File.createTempFile("tempConfig.", ".xml");
        tempConfig.deleteOnExit();
        this.resourcesDao.writeResources(config, tempConfig);
        
        final String expected = IOUtils.toString(this.getClass().getResourceAsStream("/JAXBTest/controlConfiguration.xml"));
        final String actual = FileUtils.readFileToString(tempConfig, ENCODING);
        
        final Diff d = new Diff(expected, actual);
        assertTrue(d.toString(), d.similar());
    }
    
    /**
     * Unmarshal a basic skinConfiguration.
     * 
     * @throws Exception
     */
    @Test
    public void testUnmarshalControl() throws Exception {
        final File resourceFile = new ClassPathResource("JAXBTest/controlConfiguration.xml").getFile();
        
        Resources config = this.resourcesDao.readResources(resourceFile);
        Assert.assertNotNull(config);
        Assert.assertNotNull(config.getJs());
        Assert.assertNotNull(config.getCss());
        Assert.assertEquals(1, config.getJs().size());
        Assert.assertEquals(1, config.getCss().size());
        Assert.assertEquals("/path/to/some/javascript.js", config.getJs().get(0).getValue());
        Css expected = new Css();
        expected.setValue("/path/to/some/stylesheet.css");
        config.getCss().add(expected);
        Assert.assertEquals(expected.getValue(), config.getCss().get(0).getValue());
        Assert.assertEquals("screen", config.getCss().get(0).getMedia());
    }
    
    /**
     * Marshal an basic skinConfiguration to System.out, test passes if no exception thrown.
     */
    @Test
    public void testMarshalCssWithConditional() throws Exception {
        Resources config = new Resources();
        Css css = new Css();
        css.setValue("/path/to/some/stylesheet.css");
        css.setConditional("if IE lt 7");
        config.getCss().add(css);
        
        final File tempConfig = File.createTempFile("tempConfig.", ".xml");
        tempConfig.deleteOnExit();
        this.resourcesDao.writeResources(config, tempConfig);
        
        final String expected = IOUtils.toString(this.getClass().getResourceAsStream("/JAXBTest/cssWithConditional.xml"));
        final String actual = FileUtils.readFileToString(tempConfig, ENCODING);
        
        final Diff d = new Diff(expected, actual);
        assertTrue(d.toString(), d.similar());
    }
    /**
     * Marshal an basic skinConfiguration to System.out, test passes if no exception thrown.
     */
    @Test
    public void testMarshalJsWithConditional() throws Exception {
        Resources config = new Resources();
        Js css = new Js();
        css.setValue("/path/to/some/javascript.js");
        css.setConditional("if IE lt 7");
        config.getJs().add(css);
        
        final File tempConfig = File.createTempFile("tempConfig.", ".xml");
        tempConfig.deleteOnExit();
        this.resourcesDao.writeResources(config, tempConfig);
        
        final String expected = IOUtils.toString(this.getClass().getResourceAsStream("/JAXBTest/jsWithConditional.xml"));
        final String actual = FileUtils.readFileToString(tempConfig, ENCODING);
        
        final Diff d = new Diff(expected, actual);
        assertTrue(d.toString(), d.similar());
    }
    /**
     * Marshal an basic skinConfiguration to System.out, test passes if no exception thrown.
     */
    @Test
    public void testMarshalJsCompressed() throws Exception {
        Resources config = new Resources();
        Js css = new Js();
        css.setValue("/path/to/some/javascript.js");
        css.setCompressed(true);
        config.getJs().add(css);
        
        final File tempConfig = File.createTempFile("tempConfig.", ".xml");
        tempConfig.deleteOnExit();
        this.resourcesDao.writeResources(config, tempConfig);
        
        final String expected = IOUtils.toString(this.getClass().getResourceAsStream("/JAXBTest/jsCompressed.xml"));
        final String actual = FileUtils.readFileToString(tempConfig, ENCODING);
        
        final Diff d = new Diff(expected, actual);
        assertTrue(d.toString(), d.similar());
    }
    
    /**
     * Unmarshal a resources containing a CSS with the conditional attribute
     * 
     * @throws Exception
     */
    @Test
    public void testUnmarshalCssWithConditional() throws Exception {
        final File resourceFile = new ClassPathResource("JAXBTest/cssWithConditional.xml").getFile();
        
        Resources config = this.resourcesDao.readResources(resourceFile);
    
        Assert.assertNotNull(config);
        Assert.assertNotNull(config.getJs());
        Assert.assertNotNull(config.getCss());
        Assert.assertEquals(0, config.getJs().size());
        Assert.assertEquals(1, config.getCss().size());

        Assert.assertEquals("/path/to/some/stylesheet.css", config.getCss().get(0).getValue());
        Assert.assertEquals("if IE lt 7", config.getCss().get(0).getConditional());
    }
    
    /**
     * Unmarshal a resources containing a Js with the conditional attribute
     * 
     * @throws Exception
     */
    @Test
    public void testUnmarshalJsWithConditional() throws Exception {
        final File resourceFile = new ClassPathResource("JAXBTest/jsWithConditional.xml").getFile();
        
        Resources config = this.resourcesDao.readResources(resourceFile);
    
        Assert.assertNotNull(config);
        Assert.assertNotNull(config.getJs());
        Assert.assertNotNull(config.getCss());
        Assert.assertEquals(1, config.getJs().size());
        Assert.assertEquals(0, config.getCss().size());

        Assert.assertEquals("/path/to/some/javascript.js", config.getJs().get(0).getValue());
        Assert.assertEquals("if IE lt 7", config.getJs().get(0).getConditional());
        Assert.assertFalse(config.getJs().get(0).isCompressed());
    }
    /**
     * Unmarshal a resources containing a Js with the compressed attribute
     * 
     * @throws Exception
     */
    @Test
    public void testUnmarshalJsCompressed() throws Exception {
        final File resourceFile = new ClassPathResource("JAXBTest/jsCompressed.xml").getFile();
        
        Resources config = this.resourcesDao.readResources(resourceFile);
        
        Assert.assertNotNull(config);
        Assert.assertNotNull(config.getJs());
        Assert.assertNotNull(config.getCss());
        Assert.assertEquals(1, config.getJs().size());
        Assert.assertEquals(0, config.getCss().size());

        Assert.assertEquals("/path/to/some/javascript.js", config.getJs().get(0).getValue());
        Assert.assertTrue(config.getJs().get(0).isCompressed());
    }
    
    /**
     * Order is important in resources, assert order of multiple elements
     * is preserved after JAXB unmarshalling.
     * 
     * @throws Exception
     */
    @Test
    public void testUnmarshalOrderPreserved() throws Exception {
        final File resourceFile = new ClassPathResource("JAXBTest/orderPreserved.xml").getFile();
        
        Resources config = this.resourcesDao.readResources(resourceFile);
        
        Assert.assertNotNull(config);
        Assert.assertNotNull(config.getJs());
        Assert.assertNotNull(config.getCss());
        Assert.assertEquals(6, config.getJs().size());
        Assert.assertEquals(6, config.getCss().size());
        
        for(int i = 1; i <= 6; i++) {
            Assert.assertEquals("/path/to/some/stylesheet" + i + ".css", config.getCss().get(i - 1).getValue());
        }
        
        for(int i = 1; i <= 6; i++) { 
            Assert.assertEquals("/path/to/some/javascript" + i + ".js", config.getJs().get(i - 1).getValue());
        }
        
    }
    
    /**
     * Order is important in resources, assert order of multiple elements
     * is preserved after JAXB unmarshalling.
     * 
     * @throws Exception
     */
    @Test
    public void testUnmarshalIncludes() throws Exception {
        final File resourceFile = new ClassPathResource("JAXBTest/includeScopes.xml").getFile();
        
        Resources plainConfig = this.resourcesDao.readResources(resourceFile, Included.PLAIN);
        
        Assert.assertNotNull(plainConfig);
        
        final List<Css> plainCss = plainConfig.getCss();
        Assert.assertEquals(2, plainCss.size());
        Assert.assertEquals("/path/to/some/stylesheet1.css", plainCss.get(0).getValue());
        Assert.assertEquals("stylesheet2.css", plainCss.get(1).getValue());
        
        final List<Js> plainJs = plainConfig.getJs();
        Assert.assertEquals(3, plainJs.size());
        Assert.assertEquals("/path/to/some/javascript1.js", plainJs.get(0).getValue());
        Assert.assertEquals("javascript2.js", plainJs.get(1).getValue());
        Assert.assertEquals("javascript3.js", plainJs.get(2).getValue());
        
        
        
        Resources aggrConfig = this.resourcesDao.readResources(resourceFile, Included.AGGREGATED);
        
        Assert.assertNotNull(aggrConfig);
        
        final List<Css> aggrCss = aggrConfig.getCss();
        Assert.assertEquals(2, aggrCss.size());
        Assert.assertEquals("/path/to/some/stylesheet1.min.css", aggrCss.get(0).getValue());
        Assert.assertEquals("stylesheet2.css", aggrCss.get(1).getValue());
        
        final List<Js> aggrJs = aggrConfig.getJs();
        Assert.assertEquals(3, aggrJs.size());
        Assert.assertEquals("/path/to/some/javascript1.min.js", aggrJs.get(0).getValue());
        Assert.assertEquals("javascript2.js", aggrJs.get(1).getValue());
        Assert.assertEquals("javascript3.js", aggrJs.get(2).getValue());
        
        
        
        Resources bothConfig = this.resourcesDao.readResources(resourceFile, Included.BOTH);
        
        Assert.assertNotNull(bothConfig);
        
        final List<Css> bothCss = bothConfig.getCss();
        Assert.assertEquals(3, bothCss.size());
        Assert.assertEquals("/path/to/some/stylesheet1.min.css", bothCss.get(0).getValue());
        Assert.assertEquals("/path/to/some/stylesheet1.css", bothCss.get(1).getValue());
        Assert.assertEquals("stylesheet2.css", bothCss.get(2).getValue());
        
        final List<Js> bothJs = bothConfig.getJs();
        Assert.assertEquals(4, bothJs.size());
        Assert.assertEquals("/path/to/some/javascript1.js", bothJs.get(0).getValue());
        Assert.assertEquals("/path/to/some/javascript1.min.js", bothJs.get(1).getValue());
        Assert.assertEquals("javascript2.js", bothJs.get(2).getValue());
        Assert.assertEquals("javascript3.js", bothJs.get(3).getValue());
        
    }
    

    
    /**
     * Order is important in resources, assert order of multiple elements
     * is preserved after JAXB unmarshalling.
     * 
     * @throws Exception
     */
    @Test
    public void testUnmarshalImport() throws Exception {
        final File resourceFile = new ClassPathResource("JAXBTest/importParent.xml").getFile();
        
        Resources resources = this.resourcesDao.readResources(resourceFile);
        
        Assert.assertNotNull(resources);
        
        final List<Css> css = resources.getCss();
        Assert.assertEquals(3, css.size());
        Assert.assertEquals("/path/to/some/stylesheet1.min.css", css.get(0).getValue());
        Assert.assertEquals("/path/to/some/stylesheet1.css", css.get(1).getValue());
        Assert.assertEquals("child/stylesheet2.css", css.get(2).getValue());
        
        final List<Js> js = resources.getJs();
        Assert.assertEquals(3, js.size());
        Assert.assertEquals("/path/to/some/javascript1.js", js.get(0).getValue());
        Assert.assertEquals("/path/to/some/javascript1.min.js", js.get(1).getValue());
        Assert.assertEquals("child/javascript2.js", js.get(2).getValue());
    }
    
    @Test
    public void testCssIsConditional() throws Exception {
        Css c = new Css();
        c.setValue("a.css");
        
        Assert.assertFalse(this.resourcesDao.isConditional(c));
        c.setConditional("some condition");
        Assert.assertTrue(this.resourcesDao.isConditional(c));
    }
    
    @Test
    public void testCssIsAbsolute() throws Exception {
        Css c = new Css();
        c.setValue("subdirectory/1.css");
        Assert.assertFalse(this.resourcesDao.isAbsolute(c));
        c.setValue("/subdirectory/1.css");
        Assert.assertTrue(this.resourcesDao.isAbsolute(c));
    }
    
    
    @Test
    public void testJsIsConditional() throws Exception {
        Js j = new Js();
        j.setValue("a.js");
        
        Assert.assertFalse(this.resourcesDao.isConditional(j));
        j.setConditional("some condition");
        Assert.assertTrue(this.resourcesDao.isConditional(j));
    }
    
    @Test
    public void testJsIsAbsolute() throws Exception {
        Js j = new Js();
        j.setValue("subdirectory/1.js");
        Assert.assertFalse(this.resourcesDao.isAbsolute(j));
        j.setValue("/subdirectory/1.js");
        Assert.assertTrue(this.resourcesDao.isAbsolute(j));
    }
}
