/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 18, 2009
 */
package org.globus.cog.abstraction.impl.file.coaster.buffers;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ScatteringByteChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

public class Buffers extends Thread {
    public static final Logger logger = Logger.getLogger(Buffers.class);

    public static final int ENTRY_SIZE = 32768;
    public static final int ENTRIES_PER_STREAM = 8;
    public static final int MAX_ENTRIES = 1024; // 32 MB
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
    private long lastGCTime, lastTime;
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
    }

    public static ReadBuffer newReadBuffer(Buffers buffers, ScatteringByteChannel channel, long size,
            ReadBufferCallback cb) throws InterruptedException {
        return new NIOChannelReadBuffer(buffers, channel, size, cb);
    }

    public static ReadBuffer newReadBuffer(Buffers buffers, InputStream is, long size, ReadBufferCallback cb)
            throws InterruptedException {
        return new InputStreamReadBuffer(buffers, is, size, cb);
    }

    public static WriteBuffer newWriteBuffer(Buffers buffers, File f, WriteBufferCallback cb) {
        return new NIOChannelWriteBuffer(buffers, f, cb);
    }
    
    public static WriteBuffer newEmptyFileWriteBuffer(Buffers buffers, File f, WriteBufferCallback cb) {
        return new EmptyFileWriteBuffer(buffers, f, cb);
    }
    
    public static WriteBuffer newDeleteFileWriteBuffer(Buffers buffers, File f, WriteBufferCallback cb) {
        return new DeleteFileWriteBuffer(buffers, f, cb);
    }

    public static WriteBuffer newWriteBuffer(Buffers buffers, OutputStream os, WriteBufferCallback cb) {
        return new OutputStreamWriteBuffer(buffers, os, cb);
    }

    public synchronized boolean queueRequest(boolean last, ByteBuffer buf, Buffer buffer, BufferOwner bo) {
        Entry e = new Entry(last, buf, buffer, bo);
        if (buf == null && crt > MAX_ENTRIES) {
        	if (logger.isDebugEnabled()) {
        	    logger.debug(dir + " queueRequest-waiting " + bo.getName());
        	}
            waiting.add(e);
            return true;
        }
        else {
            queue.add(e);
            if (buf == null) {
            	// not a pre-allocated buffer
            	crt++;
            	addid(bo, 1);
            	if (logger.isDebugEnabled()) {
                    logger.debug(dir + " queueRequest-queued, new buffer " + bo.getName());
                }
            }
            else {
            	if (logger.isDebugEnabled()) {
            	    logger.debug(dir + " queueRequest-queued " + bo.getName());
            	}
            }
            notify();
            return false;
        }
    }
    
    private Map<BufferOwner, Integer> mm = new HashMap<BufferOwner, Integer>();
    private Map<BufferOwner, Long> deadOwners = new HashMap<BufferOwner, Long>();
    
    private synchronized void addid(BufferOwner bo, int c) {
    	Integer i = mm.get(bo);
        if (i == null) {
            i = 0;
        }
        if (i + c == 0) {
        	mm.remove(bo);
        }
        else {
            mm.put(bo, i + c);
        }
    }
        
    public synchronized Allocation request(int count, BufferOwner bo) throws InterruptedException {
        if (logger.isDebugEnabled()) {
            logger.debug(dir + " request " + count + ", crt = " + crt + ", max = " + MAX_ENTRIES + ", owner = " + bo.getName());
        }
        while (crt >= MAX_ENTRIES) {
            if (logger.isDebugEnabled()) {
                logger.debug(dir + " max buffers reached. Waiting...");
            }
            wait();
        }
        addid(bo, count);
        crt += count;
        return new Allocation(count, bo);
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
            logger.debug(dir + " free " + alloc.count + ", crt = " + crt + ", max = " + MAX_ENTRIES + ", owner = " + alloc.bo.getName());
        }
        crt -= alloc.count;
        addid(alloc.bo, -alloc.count);
        alloc.free();
        queueWaiting();
        notify();
    }

    private void queueWaiting() {
        while (!waiting.isEmpty() && crt < MAX_ENTRIES) {
        	Entry e = waiting.removeFirst();
            queue.add(e);
            addid(e.bo, 1);
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
                        long time = System.currentTimeMillis();
                        if (time - lastTime > PERFORMANCE_LOGGING_INTERVAL) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(dir + " allocated buffers: " + formatMM(mm) + " (" + crt + ")");
                            }
                            lastTime = time;
                        }
                    }
                    e = queue.removeFirst();
                }
                try {
                    if (e.buf == null) {
                        e.buffer.doStuff(e.last, ByteBuffer.allocate(ENTRY_SIZE), new Allocation(1, e.bo));
                    }
                    else {
                        e.buffer.doStuff(e.last, e.buf, null);
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

    private String formatMM(Map<BufferOwner, Integer> mm) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Iterator<Map.Entry<BufferOwner, Integer>> i = mm.entrySet().iterator();
        while (i.hasNext()) {
        	Map.Entry<BufferOwner, Integer> e = i.next();
        	sb.append(e.getKey().getName());
        	sb.append(" -> ");
        	sb.append(e.getValue());
        	if (i.hasNext()) {
        		sb.append(", ");
        	}
        }
        sb.append("}");
        return sb.toString();
    }

    private static class Entry {
        public final ByteBuffer buf;
        public final Buffer buffer;
        public final boolean last;
        public final BufferOwner bo;

        public Entry(boolean last, ByteBuffer buf, Buffer buffer, BufferOwner bo) {
            this.last = last;
            this.buf = buf;
            this.buffer = buffer;
            this.bo = bo;
        }
    }
    
    public static class Allocation {
        private final int count;
        private boolean free;
        private final BufferOwner bo;
        
        public Allocation(int count, BufferOwner bo) {
            this.count = count;
            this.bo = bo;
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
