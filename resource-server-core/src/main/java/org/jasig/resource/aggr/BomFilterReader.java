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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Reader that trims the UTF-8 Unicon BOM from the start of a file
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class BomFilterReader extends FilterReader {
    public static final char BOM = '\uFEFF';
    private boolean trim = true;
    
    public BomFilterReader(Reader in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        int r = super.read();
        if (trim && ((char)r) == BOM) {
            return super.read();
        }
        
        trim = false;
        return r;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int r = super.read(cbuf, off, len);
        if (trim && cbuf[off] == BOM) {
            System.arraycopy(cbuf, off + 1, cbuf, off, len - 1);
            r -= 1;
            
            int newr = super.read(cbuf, off + len - 1, 1);
            if (newr > 0) {
                r += newr;
            }
        }
        
        trim = false;
        return r;
    }

}
