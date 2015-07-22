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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;

/**
 * All data written to the output stream is also used to update the provided {@link MessageDigest}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MessageDigestOutputStream extends FilterOutputStream {
    private final MessageDigest messageDigest;

    public MessageDigestOutputStream(OutputStream out, MessageDigest messageDigest) {
        super(out);
        this.messageDigest = messageDigest;
    }

    @Override
    public void write(int b) throws IOException {
        this.messageDigest.update((byte)b);
        super.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.messageDigest.update(b);
        super.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.messageDigest.update(b, off, len);
        super.write(b, off, len);
    }
}
