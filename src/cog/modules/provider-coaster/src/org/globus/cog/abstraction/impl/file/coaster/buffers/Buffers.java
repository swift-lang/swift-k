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
    
    public static enum Direction {
        IN("I"), OUT("O");
        
        private String name;
        
        Direction(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static final Buffers OUTB = new Buffers(Direction.OUT);
    private static final Buffers INB = new Buffers(Direction.IN);

    private LinkedList<Entry> queue;
    private LinkedList<Entry> waiting;
    
    private Object sizeLock = new Object();
    private int crt;
    private long lastTime, bufTime;
    private double avgBuffersUsed;
    private int maxBuffersUsed, minBuffersUsed;
    private Direction dir;
    private ThrottleManager throttleManager;
    
    public Buffers(Direction dir) {
        this.dir = dir;
        this.throttleManager = ThrottleManager.getDefault(dir);
        queue = new LinkedList<Entry>();
        waiting = new LinkedList<Entry>();
        setName("I/O Queue");
        setDaemon(true);
        start();
    }
    
    public ThrottleManager getThrottleManager() {
        return throttleManager;
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

    public static ReadBuffer newReadBuffer(Buffers buffers, ScatteringByteChannel channel, long size,
            ReadBufferCallback cb) throws InterruptedException {
        return new NIOChannelReadBuffer(buffers, channel, size, cb);
    }

    public static ReadBuffer newReadBuffer(Buffers buffers, InputStream is, long size, ReadBufferCallback cb)
            throws InterruptedException {
        return new InputStreamReadBuffer(buffers, is, size, cb);
    }

    public static WriteBuffer newWriteBuffer(Buffers buffers, GatheringByteChannel channel, WriteBufferCallback cb) {
        return new NIOChannelWriteBuffer(buffers, channel, cb);
    }

    public static WriteBuffer newWriteBuffer(Buffers buffers, OutputStream os, WriteBufferCallback cb) {
        return new OutputStreamWriteBuffer(buffers, os, cb);
    }

    public synchronized boolean queueRequest(boolean last, ByteBuffer buf, Buffer buffer) {
        Entry e = new Entry(last, buf, buffer);
        if (buf == null && crt > MAX_ENTRIES) {
            waiting.add(e);
            return true;
        }
        else {
            queue.add(e);
            if (buf == null) {
            	// not a pre-allocated buffer
            	crt++;
            }
            notify();
            return false;
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
            logger.debug(dir + " request " + count + ", crt = " + crt + ", max = " + MAX_ENTRIES);
        }
        while (crt >= MAX_ENTRIES) {
            if (logger.isDebugEnabled()) {
                logger.debug(dir + " max buffers reached. Waiting...");
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
            logger.warn(dir + " trying to release buffer allocation twice", new Exception());
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(dir + " free " + alloc.count + ", crt = " + crt + ", max = " + MAX_ENTRIES);
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
                        this.wait(ThrottleManager.MIN_UPDATE_INTERVAL);
                        throttleManager.update(MAX_ENTRIES, crt);
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
                            logger.info(dir + " elapsedTime=" + dif + ", buffersUsed[min,avg,max]="
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

    public static Buffers getBuffers(Direction dir) {
        switch(dir) {
            case IN: return INB;
            case OUT: return OUTB;
            default: return null;
        }
    }
}
