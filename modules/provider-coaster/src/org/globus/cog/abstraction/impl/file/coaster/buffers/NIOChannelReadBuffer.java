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

import org.apache.log4j.Logger;

public class NIOChannelReadBuffer extends ReadBuffer {
    public static final Logger logger = Logger.getLogger(NIOChannelReadBuffer.class);
    
    private ScatteringByteChannel channel;
    private long crt;
    private Exception ex;
    private boolean closed;

    protected NIOChannelReadBuffer(Buffers buffers, ScatteringByteChannel channel, long size,
            ReadBufferCallback cb) throws InterruptedException {
        super(buffers, cb, size);
        this.channel = channel;
        init();
    }

    public void doStuff(boolean last, ByteBuffer b, Buffers.Allocation alloc) {
    	synchronized(this) {
    		if (closed) {
    		    if (logger.isInfoEnabled()) {
    		        logger.info("Transfer done. De-allocating one unused buffer");
    		    }
    		    if (alloc != null) {
    		        buffers.free(alloc);
    		    }
    			return;
    		}
    	}
        if (alloc != null) {
            bufferCreated(alloc);
        }
        try {
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
    	synchronized(this) {
    		closed = true;
    	}
        super.close();
        channel.close();
    }
}