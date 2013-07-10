//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 26, 2009
 */
package org.globus.cog.abstraction.impl.file.coaster.buffers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;

public class NIOChannelWriteBuffer extends WriteBuffer {
    
    private GatheringByteChannel channel;
    private WriteBufferCallback cb;
    private File f;

    protected NIOChannelWriteBuffer(Buffers buffers, File f, WriteBufferCallback cb) {
    	super(buffers);
        this.f = f;
        this.cb = cb;
    }

    public void doStuff(boolean last, ByteBuffer b, Buffers.Allocation alloc) {
        try {
            if (channel == null) {
                File p = f.getParentFile();
                if (!p.exists()) {
                    if (!p.mkdirs()) {
                        throw new IOException("Failed to create directory " + p.getAbsolutePath());
                    }
                }
                channel = new FileOutputStream(f).getChannel();
            }
            channel.write(b);
            b.rewind();
            if (last) {
                channel.close();
            }
            cb.done(last);
        }
        catch (IOException e) {
            cb.error(last, e);
        }
    }
}
