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

    public static final int ENTRY_SIZE = 16384;
    public static final int ENTRIES_PER_STREAM = 16;
    public static final int MAX_ENTRIES = 1024;
    public static final int PERFORMANCE_LOGGING_INTERVAL = 10000;

    private static final Buffers INSTANCE = new Buffers();

    private LinkedList<Entry> queue;
    private Object sizeLock = new Object();
    private int crt;
    private long lastTime, bufTime;
    private double avgBuffersUsed;
    private int maxBuffersUsed, minBuffersUsed;

    public Buffers() {
        queue = new LinkedList<Entry>();
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
        queue.add(new Entry(last, buf, buffer));
        notify();
    }

    public void request(int count) throws InterruptedException {
        synchronized (sizeLock) {
            while (crt + count > MAX_ENTRIES) {
                sizeLock.wait(1000);
            }
            updateBuffersUsed();
            crt += count;
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

    public void free(int count) {
        synchronized (sizeLock) {
            updateBuffersUsed();
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
                    e = queue.removeFirst();
                }
                try {
                    e.buffer.doStuff(e.last, e.buf);
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

    public static Buffers getDefault() {
        return INSTANCE;
    }
}
