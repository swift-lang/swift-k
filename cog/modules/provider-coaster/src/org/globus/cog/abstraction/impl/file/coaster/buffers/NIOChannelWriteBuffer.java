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
import java.nio.channels.GatheringByteChannel;

public class NIOChannelWriteBuffer extends WriteBuffer {
    
    private GatheringByteChannel channel;
    private WriteBufferCallback cb;

    protected NIOChannelWriteBuffer(Buffers buffers, GatheringByteChannel channel, WriteBufferCallback cb) {
    	super(buffers);
        this.channel = channel;
        this.cb = cb;
    }

    public void doStuff(boolean last, ByteBuffer b) {
        try {
            channel.write(b);
            b.rewind();
            cb.done(last);
            if (last) {
                channel.close();
            }
        }
        catch (IOException e) {
            cb.error(last, e);
        }
    }
}
