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

import java.io.FileReader;
import java.io.Reader;
import java.net.URL;

import org.junit.Test;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class BomFilterReaderTest {
    @Test
    public void testReadWithBomNoFilter() throws Exception {
        final URL resourceUrl = this.getClass().getResource("/skin-test1/jquery.hoverIntent.bom.js");
        Reader bomFilterReader = new FileReader(resourceUrl.getFile());
        
        final int r = bomFilterReader.read();
        assertEquals(BomFilterReader.BOM, (char)r);
    }
    
    @Test
    public void testReadWithBom() throws Exception {
        final URL resourceUrl = this.getClass().getResource("/skin-test1/jquery.hoverIntent.bom.js");
        Reader bomFilterReader = new BomFilterReader(new FileReader(resourceUrl.getFile()));
        
        final int r = bomFilterReader.read();
        assertEquals('/', (char)r);
    }
    
    @Test
    public void testReadCharArrayWithBom() throws Exception {
        final URL resourceUrl = this.getClass().getResource("/skin-test1/jquery.hoverIntent.bom.js");
        Reader bomFilterReader = new BomFilterReader(new FileReader(resourceUrl.getFile()));
        
        final char[] cbuf = new char[10];
        final int r = bomFilterReader.read(cbuf);
        assertEquals(10, r);
        assertEquals("/**\n* hove", new String(cbuf));
    }
    
    @Test
    public void testReadPartialCharArrayWithBom() throws Exception {
        final URL resourceUrl = this.getClass().getResource("/skin-test1/jquery.hoverIntent.bom.js");
        Reader bomFilterReader = new BomFilterReader(new FileReader(resourceUrl.getFile()));
        
        final char[] cbuf = new char[10];
        final int r = bomFilterReader.read(cbuf, 2, 5);
        assertEquals(5, r);
        assertEquals("/**\n*", new String(cbuf, 2, 5));
    }
    
    @Test
    public void testReadCharArrayWithNoBom() throws Exception {
        final URL resourceUrl = this.getClass().getResource("/skin-test1/b.js");
        Reader bomFilterReader = new BomFilterReader(new FileReader(resourceUrl.getFile()));
        
        final char[] cbuf = new char[10];
        final int r = bomFilterReader.read(cbuf);
        assertEquals(10, r);
        assertEquals("/*\n * Lice", new String(cbuf));
    }
}
