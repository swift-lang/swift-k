//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 18, 2009
 */
package org.globus.cog.abstraction.impl.file.coaster.buffers;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.util.LinkedList;

import org.apache.log4j.Logger;

public class Buffers extends Thread {
    public static final Logger logger = Logger.getLogger(Buffers.class);

    public static final int ENTRY_SIZE = 32768;
    public static final int ENTRIES_PER_STREAM = 16;
    public static final int MAX_ENTRIES = 512; // 16 MB
    public static final int PERFORMANCE_LOGGING_INTERVAL = 10000;

    private static final Buffers INSTANCE = new Buffers();

    private LinkedList<Entry> queue;
    private LinkedList<Entry> waiting;
    
    private Object sizeLock = new Object();
    private int crt;
    private long lastTime, bufTime;
    private double avgBuffersUsed;
    private int maxBuffersUsed, minBuffersUsed;
    
    public Buffers() {
        queue = new LinkedList<Entry>();
        waiting = new LinkedList<Entry>();
        setName("I/O Queue");
        setDaemon(true);
        start();
    }

    public synchronized void start() {
        super.start();
        resetCounters();
    }

    private void resetCounters() {
        bufTime = System.currentTimeMillis();
        lastTime = bufTime;
        avgBuffersUsed = 0;
        minBuffersUsed = Integer.MAX_VALUE;
        maxBuffersUsed = 0;
    }

    public static ReadBuffer newReadBuffer(ScatteringByteChannel channel, long size,
            ReadBufferCallback cb) throws InterruptedException {
        return new NIOChannelReadBuffer(INSTANCE, channel, size, cb);
    }

    public static ReadBuffer newReadBuffer(InputStream is, long size, ReadBufferCallback cb)
            throws InterruptedException {
        return new InputStreamReadBuffer(INSTANCE, is, size, cb);
    }

    public static WriteBuffer newWriteBuffer(GatheringByteChannel channel, WriteBufferCallback cb) {
        return new NIOChannelWriteBuffer(INSTANCE, channel, cb);
    }

    public static WriteBuffer newWriteBuffer(OutputStream os, WriteBufferCallback cb) {
        return new OutputStreamWriteBuffer(INSTANCE, os, cb);
    }

    public synchronized void queueRequest(boolean last, ByteBuffer buf, Buffer buffer) {
        Entry e = new Entry(last, buf, buffer);
        if (buf == null && crt > MAX_ENTRIES) {
            waiting.add(e);
        }
        else {
            queue.add(e);
            notify();
        }
    }

    private void updateBuffersUsed() {
        long time = System.currentTimeMillis();
        avgBuffersUsed += (time - bufTime) * crt;
        if (crt > maxBuffersUsed) {
            maxBuffersUsed = crt;
        }
        if (crt < minBuffersUsed) {
            minBuffersUsed = crt;
        }
        bufTime = time;
    }
    
    public synchronized Allocation request(int count) throws InterruptedException {
        if (logger.isDebugEnabled()) {
            logger.debug("Request " + count + ", crt = " + crt + ", max = " + MAX_ENTRIES);
        }
        while (crt >= MAX_ENTRIES) {
            if (logger.isDebugEnabled()) {
                logger.debug("Max buffers reached. Waiting...");
            }
            wait();
        }
        updateBuffersUsed();
        crt += count;
        return new Allocation(count);
    }

    public synchronized void free(Allocation alloc) {
        if (alloc == null) {
            throw new IllegalArgumentException("Null alloc");
        }
        if (alloc.free) {
            logger.warn("Trying to release buffer allocation twice", new Exception());
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Free " + alloc.count + ", crt = " + crt + ", max = " + MAX_ENTRIES);
        }
        updateBuffersUsed();
        crt -= alloc.count;
        alloc.free();
        queueWaiting();
        notify();
    }

    private void queueWaiting() {
        while (!waiting.isEmpty() && crt < MAX_ENTRIES) {
            queue.add(waiting.removeFirst());
            crt++;
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
                    e = queue.removeFirst();
                }
                try {
                    if (e.buf == null) {
                        e.buffer.doStuff(e.last, ByteBuffer.allocate(ENTRY_SIZE), new Allocation(1));
                    }
                    else {
                        e.buffer.doStuff(e.last, e.buf, null);
                    }
                    if (logger.isInfoEnabled()) {
                        long time = System.currentTimeMillis();
                        long dif = time - lastTime;
                        if (dif > PERFORMANCE_LOGGING_INTERVAL) {
                            int avgbuf = (int) (avgBuffersUsed / dif);
                            logger.info("elapsedTime=" + dif + ", buffersUsed[min,avg,max]="
                                    + minBuffersUsed + ", " + avgbuf + ", " + maxBuffersUsed);
                            resetCounters();
                        }
                    }
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
    
    public static class Allocation {
        private int count;
        private boolean free;
        
        public Allocation(int count) {
            this.count = count;
        }
        
        public void free() {
            free = true;
        }
    }

    public static Buffers getDefault() {
        return INSTANCE;
    }
}
