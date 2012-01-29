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

import org.apache.log4j.Logger;



public abstract class ReadBuffer extends Buffer {
    public static final Logger logger = Logger.getLogger(ReadBuffer.class);
    
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
        
        int nbuf = Math.min((int) (size / Buffers.ENTRY_SIZE) + 1, Buffers.ENTRIES_PER_STREAM);
        
        if (logger.isInfoEnabled()) {
            logger.info("Will ask for " + nbuf + " buffers for a size of " + size);
        }
        
        for (int i = 0; i < nbuf; i++) {
            // these will be allocated when the first read happens,
            // which also happens to happen in the I/O thread
            empty.add(null);
        }
        if (requestFill() == nbuf) {
            // all buffers are queued
            cb.queued();
        }
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

    protected int requestFill() {
        int queued = 0;
        synchronized (empty) {
            while (!empty.isEmpty() && read < size) {
                ByteBuffer buf = empty.removeFirst();
                if (buf != null) {
                    buf.clear();
                }
                if (buffers.queueRequest(false, buf, this)) {
                    queued++;
                }
            }
        }
        return queued;
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
        if (logger.isDebugEnabled()) {
            logger.debug("buffer created");
        }
        allocs.add(a);
    }
    
    protected void deallocateBuffers() {
        if (logger.isInfoEnabled()) {
            logger.info("De-allocating " + allocs.size() + " buffers");
        }
        for (Buffers.Allocation a : allocs) {
            buffers.free(a);
        }
    }
    
    public void close() throws IOException {
        super.close();
        deallocateBuffers();
    }
}
