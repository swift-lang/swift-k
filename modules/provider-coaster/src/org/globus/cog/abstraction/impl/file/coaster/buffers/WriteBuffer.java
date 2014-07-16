//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 4, 2010
 */
package org.globus.cog.abstraction.impl.file.coaster.buffers;

import java.nio.ByteBuffer;


public abstract class WriteBuffer extends Buffer implements BufferOwner {

    protected WriteBuffer(Buffers buffers) {
        super(buffers);
    }

    public void write(boolean last, byte[] data) throws InterruptedException {
        buffers.queueRequest(last, ByteBuffer.wrap(data), this, this);
    }

    public String getName() {
        return "WB";
    }

    public boolean isAlive() {
        return true;
    }
}
