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