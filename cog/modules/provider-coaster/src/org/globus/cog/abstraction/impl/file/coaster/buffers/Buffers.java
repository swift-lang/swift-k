//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 18, 2009
 */
package org.globus.cog.abstraction.impl.file.coaster.buffers;

import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.util.LinkedList;

import org.apache.log4j.Logger;

public class Buffers extends Thread {
    public static final Logger logger = Logger.getLogger(Buffers.class);

    public static final int ENTRY_SIZE = 16384;
    public static final int ENTRIES_PER_STREAM = 16;
    public static final int MAX_ENTRIES = 1024;

    private static final Buffers INSTANCE = new Buffers();

    private LinkedList queue;
    private Object sizeLock = new Object();
    private int crt;

    public Buffers() {
        queue = new LinkedList();
        setName("I/O Queue");
        setDaemon(true);
        start();
    }

    public static ReadBuffer newReadBuffer(ScatteringByteChannel channel, long size,
            ReadBufferCallback cb) throws InterruptedException {
        return new NIOChannelReadBuffer(INSTANCE, channel, size, cb);
    }

    public static WriteBuffer newWriteBuffer(GatheringByteChannel channel, WriteBufferCallback cb) {
        return new NIOChannelWriteBuffer(INSTANCE, channel, cb);
    }

    public synchronized void queueRequest(boolean last, ByteBuffer buf, Buffer buffer) {
        queue.add(new Entry(last, buf, buffer));
        notify();
    }

    public void request(int count) throws InterruptedException {
        synchronized (sizeLock) {
            while (crt + count > MAX_ENTRIES) {
                sizeLock.wait(1000);
            }
            crt += count;
        }
    }

    public void free(int count) {
        synchronized (sizeLock) {
            crt -= count;
            sizeLock.notify();
        }
    }

    public void run() {
        try {
            while (true) {
                Entry e;
                synchronized (this) {
                    while (queue.isEmpty()) {
                        this.wait();
                    }
                    e = (Entry) queue.removeFirst();
                }
                try {
                    e.buffer.doStuff(e.last, e.buf);
                }
                catch (Exception ex) {
                    logger.error(e.buffer.getClass() + " throws exception in doStuff. Fix it!", ex);
                }
            }
        }
        catch (InterruptedException e) {
        }
    }

    private static class Entry {
        public final ByteBuffer buf;
        public final Buffer buffer;
        public final boolean last;

        public Entry(boolean last, ByteBuffer buf, Buffer buffer) {
            this.last = last;
            this.buf = buf;
            this.buffer = buffer;
        }
    }

    public static Buffers getDefault() {
        return INSTANCE;
    }
}
