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

public class EmptyFileWriteBuffer extends WriteBuffer {
    
    private static final ByteBuffer EMPTY_BB = ByteBuffer.allocate(0);
    
    private WriteBufferCallback cb;
    private File f;

    protected EmptyFileWriteBuffer(Buffers buffers, File f, WriteBufferCallback cb) {
    	super(buffers);
        this.f = f;
        this.cb = cb;
        buffers.queueRequest(true, EMPTY_BB, this, this);
    }

    public void doStuff(boolean last, ByteBuffer b, Buffers.Allocation alloc) {
        try {
            File p = f.getParentFile();
            if (!p.exists()) {
                if (!p.mkdirs()) {
                    throw new IOException("Failed to create directory " + p.getAbsolutePath());
                }
            }
            new FileOutputStream(f).close();
            cb.done(true);
        }
        catch (IOException e) {
            cb.error(last, e);
        }
    }
}
