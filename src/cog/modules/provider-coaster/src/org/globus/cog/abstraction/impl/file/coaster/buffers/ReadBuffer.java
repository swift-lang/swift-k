//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 4, 2010
 */
package org.globus.cog.abstraction.impl.file.coaster.buffers;



public abstract class ReadBuffer extends Buffer {
    private final ReadBufferCallback cb;

    protected ReadBuffer(Buffers buffers, ReadBufferCallback cb) {
        super(buffers);
        this.cb = cb;
    }

    public abstract void freeFirst();
    
    protected ReadBufferCallback getCallback() {
        return cb;
    }
}
