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
import java.io.FileReader;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * Test harness for {@link ResourcesAggregatorImpl}.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class ResourcesAggregatorImplTest {
    @BeforeClass
    public static void xmlUnitInit() {
        XMLUnit.setIgnoreWhitespace(true);
    }

	@Test
	public void testControl() throws Exception {
		String tempPath = getTestOutputRoot() + "/skin-test1";

		File outputDirectory = new File(tempPath);
		outputDirectory.mkdirs();
		Assert.assertTrue(outputDirectory.exists());

		File skinXml = new ClassPathResource("skin-test1/skin.xml").getFile();
		Assert.assertTrue(skinXml.exists());

		ResourcesAggregatorImpl impl = new ResourcesAggregatorImpl();
		impl.aggregate(skinXml, outputDirectory);
		
		Diff d = new Diff(
		        new FileReader(new ClassPathResource("skin-test1/skin.aggr.xml").getFile()), 
		        new FileReader(new File(outputDirectory, "skin.aggr.xml")));
        assertTrue(d.toString(), d.similar());
	}

	@Test
	public void testAllAbsolute() throws Exception {
		String tempPath = getTestOutputRoot() + "/skin-testAllAbsolute";

		File outputDirectory = new File(tempPath);
		outputDirectory.mkdirs();
		Assert.assertTrue(outputDirectory.exists());

		File skinXml = new ClassPathResource("skin-testAllAbsolute/skin.xml").getFile();
		Assert.assertTrue(skinXml.exists());

		ResourcesAggregatorImpl impl = new ResourcesAggregatorImpl();
		impl.aggregate(skinXml, outputDirectory);
		
		Diff d = new Diff(
                new FileReader(new ClassPathResource("skin-testAllAbsolute/skin.aggr.xml").getFile()), 
                new FileReader(new File(outputDirectory, "skin.aggr.xml")));
        assertTrue(d.toString(), d.similar());
	}

	@Test
	public void testComplex() throws Exception {
		String tempPath = getTestOutputRoot() + "/skin-complex/superskin";
		
		File outputDirectory = new File(tempPath);
		outputDirectory.mkdirs();
		Assert.assertTrue(outputDirectory.exists());

		File skinXml = new ClassPathResource("skin-complex/superskin/skin.xml").getFile();
		Assert.assertTrue(skinXml.exists());

		ResourcesAggregatorImpl impl = new ResourcesAggregatorImpl();
		impl.aggregate(skinXml, outputDirectory);
        
        Diff d = new Diff(
                new FileReader(new ClassPathResource("skin-complex/superskin/skin.aggr.xml").getFile()), 
                new FileReader(new File(outputDirectory, "skin.aggr.xml")));
        assertTrue(d.toString(), d.similar());
	}

    @Test
    public void testUniversalityCommonJavaScript() throws Exception {
        String tempPath = getTestOutputRoot() + "/skin-universality-commonjs/uportal3";
        
        File outputDirectory = new File(tempPath);
        outputDirectory.mkdirs();
        Assert.assertTrue(outputDirectory.exists());
        
        String jsPath = getTestOutputRoot() + "/skin-universality-commonjs/common/javascript";
        File jsOutputDirectory = new File(jsPath);

        File skinXml = new ClassPathResource("skin-universality-commonjs/uportal3/skin.xml").getFile();
        Assert.assertTrue(skinXml.exists());

        ResourcesAggregatorImpl impl = new ResourcesAggregatorImpl();
        impl.aggregate(skinXml, outputDirectory, jsOutputDirectory);
        
        Diff d = new Diff(
                new FileReader(new ClassPathResource("skin-universality-commonjs/uportal3/skin.aggr.xml").getFile()), 
                new FileReader(new File(outputDirectory, "skin.aggr.xml")));
        assertTrue(d.toString(), d.similar());
    }

	@Test
	public void testUniversality() throws Exception {
		String tempPath = getTestOutputRoot() + "/skin-universality/uportal3";
		
		File outputDirectory = new File(tempPath);
		outputDirectory.mkdirs();
		Assert.assertTrue(outputDirectory.exists());

		File skinXml = new ClassPathResource("skin-universality/uportal3/skin.xml").getFile();
		Assert.assertTrue(skinXml.exists());

        ResourcesAggregatorImpl impl = new ResourcesAggregatorImpl();
        impl.aggregate(skinXml, outputDirectory);
        
        Diff d = new Diff(
                new FileReader(new ClassPathResource("skin-universality/uportal3/skin.aggr.xml").getFile()), 
                new FileReader(new File(outputDirectory, "skin.aggr.xml")));
        assertTrue(d.toString(), d.similar());
	}

    @Test
    public void testUniversalityImports() throws Exception {
        String tempPath = getTestOutputRoot() + "/skin-universality-imports/uportal3";
        
        File outputDirectory = new File(tempPath);
        outputDirectory.mkdirs();
        Assert.assertTrue(outputDirectory.exists());

        File skinXml = new ClassPathResource("skin-universality-imports/uportal3/skin.xml").getFile();
        Assert.assertTrue(skinXml.exists());

        ResourcesAggregatorImpl impl = new ResourcesAggregatorImpl();
        impl.aggregate(skinXml, outputDirectory);
        
        Diff d = new Diff(
                new FileReader(new ClassPathResource("skin-universality-imports/uportal3/skin.aggr.xml").getFile()), 
                new FileReader(new File(outputDirectory, "skin.aggr.xml")));
        assertTrue(d.toString(), d.similar());
    }

	/**
	 * Delete our temp directory after test execution.
	 * @throws Exception
	 */
//	@After
	public void cleanupTempDir() throws Exception {
		File testOutputDirectory = new File(getTestOutputRoot());
		FileUtils.cleanDirectory(testOutputDirectory);
		FileUtils.deleteDirectory(testOutputDirectory);
	}

	/**
	 * Shortcut to get a temporary directory underneath java.io.tmpdir.
	 * 
	 * @return
	 */
	private String getTestOutputRoot() {
		String tempPath = System.getProperty("java.io.tmpdir");
		if(!tempPath.endsWith("/")) {
			tempPath += "/";
		}
		tempPath = tempPath + "resources-aggregator-impl-test-output";
		return tempPath;
	}
}
