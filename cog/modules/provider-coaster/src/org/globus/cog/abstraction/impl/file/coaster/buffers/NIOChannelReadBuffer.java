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
import java.nio.ByteBuffer;
import java.nio.channels.ScatteringByteChannel;

public class NIOChannelReadBuffer extends ReadBuffer {
    private ScatteringByteChannel channel;
    private long crt;
    private Exception ex;

    protected NIOChannelReadBuffer(Buffers buffers, ScatteringByteChannel channel, long size,
            ReadBufferCallback cb) throws InterruptedException {
        super(buffers, cb, size);
        this.channel = channel;
        init();
    }

    public void doStuff(boolean last, ByteBuffer b) {
        if (read >= size) {
            return;
        }
        try {
            if (b == null) {
                b = allocateOneBuffer();
            }
            channel.read(b);
            b.limit(b.position());
            b.rewind();
            bufferRead(b);
        }
        catch (Exception ex) {
            error(b, ex);
        }
    }

    public void close() throws IOException {
        super.close();
        channel.close();
    }
}