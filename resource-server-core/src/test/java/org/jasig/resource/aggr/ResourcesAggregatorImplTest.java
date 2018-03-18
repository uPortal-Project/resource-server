/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
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
    public void testIncludeOverlay() throws Exception {
        String tempPath = getTestOutputRoot() + "/skin-test-incl-overlay";

        File outputDirectory = new File(tempPath);
        outputDirectory.mkdirs();
        Assert.assertTrue(outputDirectory.exists());

        FileUtils.copyFileToDirectory(new ClassPathResource("skin-test-incl-overlay/overlay/overlay.css").getFile(), outputDirectory);
        FileUtils.copyFileToDirectory(new ClassPathResource("skin-test-incl-overlay/overlay/overlay.js").getFile(), outputDirectory);

        File skinXml = new ClassPathResource("skin-test-incl-overlay/skin.xml").getFile();
        Assert.assertTrue(skinXml.exists());

        ResourcesAggregatorImpl impl = new ResourcesAggregatorImpl();
        impl.aggregate(new AggregationRequest()
            .setResourcesXml(skinXml)
            .setOutputBaseDirectory(outputDirectory)
            .addAdditionalSourceDirectory(outputDirectory));

        Diff d = new Diff(
                new FileReader(new ClassPathResource("skin-test-incl-overlay/skin.aggr.xml").getFile()),
                new FileReader(new File(outputDirectory, "skin.aggr.xml")));
        assertTrue(d.toString(), d.similar());
    }

    @Test(expected=IOException.class)
    public void testMissingIncludeOverlay() throws Exception {
        String tempPath = getTestOutputRoot() + "/skin-test-incl-overlay";

        File outputDirectory = new File(tempPath);
        outputDirectory.mkdirs();
        Assert.assertTrue(outputDirectory.exists());

        File skinXml = new ClassPathResource("skin-test-incl-overlay/skin.xml").getFile();
        Assert.assertTrue(skinXml.exists());

        ResourcesAggregatorImpl impl = new ResourcesAggregatorImpl();
        impl.aggregate(new AggregationRequest()
            .setResourcesXml(skinXml)
            .setOutputBaseDirectory(outputDirectory)
            .addAdditionalSourceDirectory(outputDirectory));
    }

	/**
	 * Delete our temp directory after test execution.
	 * @throws Exception
	 */
	@After
	public void cleanupTempDir() throws Exception {
	    String testOutputDirectoryName = tempDirLocal.get();
	    tempDirLocal.remove();
		File testOutputDirectory = new File(testOutputDirectoryName);
		FileUtils.cleanDirectory(testOutputDirectory);
		FileUtils.deleteDirectory(testOutputDirectory);
	}

	private final ThreadLocal<String> tempDirLocal = new ThreadLocal<String>();

	/**
	 * Shortcut to get a temporary directory underneath java.io.tmpdir.
	 */
	private String getTestOutputRoot() throws IOException {
	    String testOutputDirectoryName = tempDirLocal.get();
	    if (testOutputDirectoryName != null) {
	        return testOutputDirectoryName;
	    }

		final File tmpDir = File.createTempFile("resources-aggregator-impl-", "-test-output");
		tmpDir.delete();
		tmpDir.mkdirs();
		tmpDir.deleteOnExit();
		testOutputDirectoryName = tmpDir.getCanonicalPath();
		tempDirLocal.set(testOutputDirectoryName);

		return testOutputDirectoryName;
	}
}
