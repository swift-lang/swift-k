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

public abstract class Buffer {
	protected final Buffers buffers;
    protected Buffer(Buffers buffers) {
    	this.buffers = buffers;
    }

    public abstract void doStuff(boolean last, ByteBuffer b, Buffers.Allocation alloc);

    public void close() throws IOException {
    }
    
    
    protected byte[] toByteArray(ByteBuffer b) {
        if (b.hasArray() && (b.limit() == b.capacity())) {
            return b.array();
        }
        else {
            byte[] bbuf = new byte[b.limit()];
            b.get(bbuf);
            return bbuf;
        }
    }
}