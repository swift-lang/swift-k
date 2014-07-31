/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 26, 2009
 */
package org.globus.cog.abstraction.impl.file.coaster.buffers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class InputStreamReadBuffer extends ReadBuffer {
    private InputStream is;
    // private long crt;
    // private Exception ex;

    protected InputStreamReadBuffer(Buffers buffers, InputStream is, long size,
            ReadBufferCallback cb) throws InterruptedException {
        super(buffers, cb, size);
        this.is = is;
        init();
    }

    public void doStuff(boolean last, ByteBuffer b, Buffers.Allocation alloc) {
        if (read >= size) {
            return;
        }
        if (alloc != null) {
            bufferCreated(alloc);
        }
        try {
            if (b.hasArray()) {
                int len = is.read(b.array());
                b.limit(len);
            }
            else {
                byte[] buf = new byte[b.capacity()];
                int len = is.read(buf);
                b.put(buf, 0, len);
                b.limit(len);
            }
            b.rewind();
            bufferRead(b);
        }
        catch (Exception ex) {
            error(b, ex);
        }
    }

    public void close() throws IOException {
        super.close();
        is.close();
    }
}