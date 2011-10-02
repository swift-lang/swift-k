//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 4, 2010
 */
package org.globus.cog.abstraction.impl.file.coaster.buffers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;



public abstract class ReadBuffer extends Buffer {
    private final ReadBufferCallback cb;
    protected LinkedList<ByteBuffer> full;
    protected LinkedList<ByteBuffer> empty;
    protected long read;
    protected long size;
    protected List<Buffers.Allocation> allocs;

    protected ReadBuffer(Buffers buffers, ReadBufferCallback cb, long size) {
        super(buffers);
        this.cb = cb;
        this.size = size;
    }
    
    protected ReadBufferCallback getCallback() {
        return cb;
    }

    protected void init() throws InterruptedException {
        full = new LinkedList<ByteBuffer>();
        empty = new LinkedList<ByteBuffer>();
        allocs = new ArrayList<Buffers.Allocation>();
        for (int i = 0; i < Buffers.ENTRIES_PER_STREAM; i++) {
            // these will be allocated when the first read happens,
            // which also happens to happen in the I/O thread
            empty.add(null);
        }
        requestFill();
    }
    
    public void freeFirst() {
        ByteBuffer b;
        synchronized (this) {
            b = full.removeFirst();
            b.clear();
        }
        buffers.queueRequest(false, b, this);
        requestFill();
    }

    protected void requestFill() {
        synchronized (empty) {
            while (!empty.isEmpty() && read < size) {
                ByteBuffer buf = empty.removeFirst();
                if (buf != null) {
                    buf.clear();
                }
                buffers.queueRequest(false, buf, this);
            }
        }
    }

    public void error(ByteBuffer buf, Exception e) {
        synchronized (empty) {
            empty.addLast(buf);
            getCallback().error(false, e);
        }
    }

    public void bufferRead(ByteBuffer buf) {
        synchronized(this) {
            read += buf.limit();
            full.addLast(buf);
        }
        getCallback().dataRead(read == size, buf);
    }
    
    protected void bufferCreated(Buffers.Allocation a) {
        allocs.add(a);
    }
    
    protected void deallocateBuffers() {
        for (Buffers.Allocation a : allocs) {
            buffers.free(a);
        }
    }
    
    public void close() throws IOException {
        super.close();
        deallocateBuffers();
    }
}
