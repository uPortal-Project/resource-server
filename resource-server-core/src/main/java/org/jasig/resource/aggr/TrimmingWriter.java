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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Performs the same function as {@link String#trim()} on the whole of the input without having to buffer the entire
 * input in memory.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TrimmingWriter extends FilterWriter {
    private final StringBuilder trimEndBuffer = new StringBuilder();
    private boolean trimmingStart = true;
    private int charCount = 0;

    public TrimmingWriter(Writer out) {
        super(out);
    }
    
    /**
     * Number of characters writtent post-trimming
     */
    public int getCharCount() {
        return charCount;
    }

    @Override
    public void write(int c) throws IOException {
        if (trimmingStart) {
            if (Character.isWhitespace(c)) {
                return;
            }
            
            trimmingStart = false;
        }
        else if (Character.isWhitespace(c)) {
            trimEndBuffer.append((char)c);
            return;
        }
        else if (trimEndBuffer.length() > 0) {
            flushBuffer();
        }
        
        charCount++;
        super.write(c);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        //Trimming whitespace off the start
        if (trimmingStart) {
            int idx = 0;
            for (; trimmingStart && idx < len; idx++) {
                if (!Character.isWhitespace(cbuf[idx + off])) {
                    trimmingStart = false;
                    off += idx;
                    len -= idx;
                }
            }
            
            if (idx == len) {
                return;
            }
        }
        
        //Trimming whitespace off the end
        int idx = 0;
        while (idx < len && Character.isWhitespace(cbuf[off + len - idx - 1])) {
            idx++;
        }
        
        //Unless the provided data was completely whitespace flush any buffered data
        if (idx != len && trimEndBuffer.length() > 0) {
            flushBuffer();
        }
        
        //If there was whitespace at the end of the provided data buffer it for future use
        if (idx > 0) {
            //String buffer appends strings with start/end but char[] with start/len
            trimEndBuffer.append(cbuf, off + len - idx, idx);
            len -= idx;
        }
        
        //Write out whats left
        charCount += len;
        super.write(cbuf, off, len);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        //Trimming whitespace off the start
        if (trimmingStart) {
            int idx = 0;
            for (; trimmingStart && idx < len; idx++) {
                if (!Character.isWhitespace(str.charAt(idx + off))) {
                    trimmingStart = false;
                    off += idx;
                    len -= idx;
                }
            }
            
            if (idx == len) {
                return;
            }
        }
        
        //Trimming whitespace off the end
        int idx = 0;
        while (idx < len && Character.isWhitespace(str.charAt(off + len - idx - 1))) {
            idx++;
        }
        
        //Unless the provided data was completely whitespace flush any buffered data
        if (idx != len && trimEndBuffer.length() > 0) {
            flushBuffer();
        }
        
        //If there was whitespace at the end of the provided data buffer it for future use
        if (idx > 0) {
            //String buffer appends strings with start/end but char[] with start/len
            trimEndBuffer.append(str, off + len - idx, off + len);
            len -= idx;
        }
        
        //Write out whats left
        charCount += len;
        super.write(str, off, len);
    }
    
    @Override
    public void close() throws IOException {
        this.trimEndBuffer.setLength(0);
        this.trimEndBuffer.trimToSize();
        super.close();
    }

    protected void flushBuffer() throws IOException {
        final String buffer = trimEndBuffer.toString();
        final int bufferLength = buffer.length();
        super.write(buffer, 0, bufferLength);
        charCount += bufferLength;
        trimEndBuffer.setLength(0);
    }
}