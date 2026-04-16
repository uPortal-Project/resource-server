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
package org.jasig.resource.aggr;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

public class EsbuildCompressorTest {

    @Test
    public void testJavaScriptIsMinified() throws Exception {
        final String input = IOUtils.toString(
                new ClassPathResource("skin-test1/a.js").getInputStream(), StandardCharsets.UTF_8);
        final StringWriter output = new StringWriter();

        EsbuildCompressor.compressJavaScript(new StringReader(input), output);

        final String minified = output.toString();
        assertTrue("Minified JS should be smaller than input", minified.length() < input.length());
        assertFalse("Minified JS should not contain block comments", minified.contains("/*"));
        assertTrue("Minified JS should preserve function body", minified.contains("alert("));
    }

    @Test
    public void testCssIsMinified() throws Exception {
        final String input = IOUtils.toString(
                new ClassPathResource("skin-test1/a.css").getInputStream(), StandardCharsets.UTF_8);
        final StringWriter output = new StringWriter();

        EsbuildCompressor.compressCss(new StringReader(input), output);

        final String minified = output.toString();
        assertTrue("Minified CSS should be smaller than input", minified.length() < input.length());
        assertFalse("Minified CSS should not contain block comments", minified.contains("/*"));
        assertTrue("Minified CSS should preserve selector", minified.contains(".selector"));
    }
}
