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

import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TrimmingWriterTest {
    @Test
    public void testTrimmingStringWriter() throws IOException {
        final StringWriter output = new StringWriter();
        final TrimmingWriter trimmingWriter = new TrimmingWriter(output);
        
        trimmingWriter.write(" start ");
        trimmingWriter.write("  ");
        trimmingWriter.write(" mid dle ");
        trimmingWriter.write("  ");
        trimmingWriter.write(" end ");
        
        final String expected = "start    mid dle    end";
        assertEquals(expected, output.toString());
        assertEquals(expected.length(), trimmingWriter.getCharCount());
    }
    
    @Test
    public void testTrimmingCharacterWriter() throws IOException {
        final StringWriter output = new StringWriter();
        final TrimmingWriter trimmingWriter = new TrimmingWriter(output);
        
        trimmingWriter.write(" start ".toCharArray());
        trimmingWriter.write("  ".toCharArray());
        trimmingWriter.write(" mid dle ".toCharArray());
        trimmingWriter.write("  ".toCharArray());
        trimmingWriter.write(" end ".toCharArray());
        
        final String expected = "start    mid dle    end";
        assertEquals(expected, output.toString());
        assertEquals(expected.length(), trimmingWriter.getCharCount());
    }
    
    @Test
    public void testTrimmingMixedWriter() throws IOException {
        final StringWriter output = new StringWriter();
        final TrimmingWriter trimmingWriter = new TrimmingWriter(output);
        
        trimmingWriter.write(" start ");
        trimmingWriter.write("  ".toCharArray());
        trimmingWriter.write(" mid ");
        trimmingWriter.write(' ');
        trimmingWriter.write('-');
        trimmingWriter.write(' ');
        trimmingWriter.write(" dle ".toCharArray());
        trimmingWriter.write("  ");
        trimmingWriter.write(" end ".toCharArray());
        
        final String expected = "start    mid  -  dle    end";
        assertEquals(expected, output.toString());
        assertEquals(expected.length(), trimmingWriter.getCharCount());
    }
}
