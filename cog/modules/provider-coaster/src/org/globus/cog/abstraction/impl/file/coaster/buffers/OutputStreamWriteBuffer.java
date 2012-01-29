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
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class OutputStreamWriteBuffer extends WriteBuffer {
    
    private OutputStream os;
    private WriteBufferCallback cb;

    protected OutputStreamWriteBuffer(Buffers buffers, OutputStream os, WriteBufferCallback cb) {
    	super(buffers);
        this.os = os;
        this.cb = cb;
    }

    public void doStuff(boolean last, ByteBuffer b, Buffers.Allocation alloc) {
        try {
            os.write(toByteArray(b));
            b.rewind();
            cb.done(last);
            if (last) {
                os.close();
            }
        }
        catch (IOException e) {
            cb.error(last, e);
        }
    }
}
