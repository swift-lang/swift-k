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
import java.util.LinkedList;

public class NIOChannelReadBuffer extends ReadBuffer {
    private ScatteringByteChannel channel;
    private long size, crt, read;
    protected LinkedList full;
    private LinkedList empty;
    private Exception ex;

    protected NIOChannelReadBuffer(Buffers buffers, ScatteringByteChannel channel, long size,
            ReadBufferCallback cb) throws InterruptedException {
        super(buffers, cb);
        this.channel = channel;
        this.size = size;
        init();
    }

    public void doStuff(boolean last, ByteBuffer b) {
        if (read >= size) {
            return;
        }
        try {
            channel.read(b);
            b.limit(b.position());
            b.rewind();
            bufferRead(b);
        }
        catch (IOException ex) {
            error(b, ex);
        }
    }

    public void error(ByteBuffer buf, IOException e) {
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

    public void close() throws IOException {
        super.close();
        buffers.free(Buffers.ENTRIES_PER_STREAM);
        channel.close();
    }

    protected void init() throws InterruptedException {
        full = new LinkedList();
        empty = new LinkedList();
        buffers.request(Buffers.ENTRIES_PER_STREAM);
        for (int i = 0; i < Buffers.ENTRIES_PER_STREAM; i++) {
            empty.add(ByteBuffer.allocate(Buffers.ENTRY_SIZE));
        }
        requestFill();
    }

    public void freeFirst() {
        ByteBuffer b;
        synchronized (this) {
            b = (ByteBuffer) full.removeFirst();
            b.clear();
        }
        buffers.queueRequest(false, b, this);
        requestFill();
    }

    private void requestFill() {
        synchronized (empty) {
            while (!empty.isEmpty() && read < size) {
                ByteBuffer buf = (ByteBuffer) empty.removeFirst();
                buf.clear();
                buffers.queueRequest(false, buf, this);
            }
        }
    }
}