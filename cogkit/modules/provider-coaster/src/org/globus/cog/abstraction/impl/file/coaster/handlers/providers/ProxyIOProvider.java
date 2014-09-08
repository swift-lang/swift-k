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
 * Created on Jan 1, 2010
 */
package org.globus.cog.abstraction.impl.file.coaster.handlers.providers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.file.coaster.buffers.Buffers;
import org.globus.cog.abstraction.impl.file.coaster.buffers.Buffers.Allocation;
import org.globus.cog.abstraction.impl.file.coaster.buffers.Buffers.Direction;
import org.globus.cog.abstraction.impl.file.coaster.buffers.ReadBuffer;
import org.globus.cog.abstraction.impl.file.coaster.buffers.ReadBufferCallback;
import org.globus.cog.abstraction.impl.file.coaster.buffers.ThrottleManager;
import org.globus.cog.abstraction.impl.file.coaster.buffers.WriteBuffer;
import org.globus.cog.abstraction.impl.file.coaster.commands.GetFileCommand;
import org.globus.cog.abstraction.impl.file.coaster.commands.PutFileCommand;
import org.globus.cog.abstraction.impl.file.coaster.handlers.GetFileHandler;
import org.globus.cog.abstraction.impl.file.coaster.handlers.PutFileHandler;
import org.globus.cog.abstraction.interfaces.RemoteFile;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.channels.ChannelManager;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.commands.Command.Callback;

// import edu.emory.mathcs.backport.java.util.concurrent.BlockingQueue;
// import edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingQueue;

public class ProxyIOProvider implements IOProvider {
    public static final Logger logger = Logger.getLogger(ProxyIOProvider.class);

    public void abort(IOHandle handle) throws IOException {
        ((Abortable) handle).abort();
    }

    public boolean isDirect() {
        return true;
    }

    public IOReader pull(String src, String dest, ReadIOCallback cb) throws IOException {
        return new Reader(src, dest, cb);
    }

    public IOWriter push(String src, String dest, WriteIOCallback cb) throws IOException {
        return new Writer(src, dest, cb);
    }

    private static class Writer implements IOWriter, Callback {
        /**
         * Reverse the buffer direction compared to the local IO provider.
         * The actual label on the set of buffers is not relevant as long as
         * they are different on one JVM instance. But when using proxy mode
         * in local:local (i.e. both service and client in the same JVM) this
         * avoids a deadlock.
         */
        private static Direction BUFDIR = Direction.IN;
        
        private CustomPutFileCmd cmd;
        private WriteIOCallback cb;
        private CoasterChannel channel;
        private String src, dst;
        private boolean done, suspended;

        public Writer(String src, String dst, WriteIOCallback cb) throws IOException {
            this.cb = cb;
            this.src = src;
            this.dst = dst;
        }
        
        public String toString() {
            return "PW " + src + " -> " + dst;
        }

        public void close() throws IOException {
        }

        public void setLength(long len) throws IOException {
            try {
                RemoteFile uri = new RemoteFile(dst);
                cmd = new CustomPutFileCmd(src, "file://localhost/" + uri.getPath(), len, this);
                channel = ChannelManager.getManager().getExistingChannel("id://" + uri.getHost(), null);
                cmd.executeAsync(channel, this);
                cb.info(String.valueOf(cmd.getId()));
                synchronized(this) {
                    if (!suspended) {
                        return;
                    }
                }
                if (logger.isInfoEnabled()) {
                    logger.info(cmd.getId() + " suspended before. Sending signal.");
                }
                cmd.suspend();
            }
            catch (Exception e) {
                throw new IOException(e.getMessage());
            }
        }

        public void write(boolean last, byte[] data) throws IOException {
            try {
                done = last;
                cmd.getBuffer().queue(last, ByteBuffer.wrap(data));
            }
            catch (InterruptedException e) {
                cmd.getBuffer().error(e);
            }
        }

        public void errorReceived(Command cmd, String msg, Exception t) {
            cb.error(this, t);
        }

        public void replyReceived(Command cmd) {
            cb.done(this);
        }

        public void abort() throws IOException {
            close();
        }

        public void suspend() {
            if (!done) {
                synchronized(this) {
                    if (cmd == null) {
                        suspended = true;
                        return;
                    }
                }
                cmd.suspend();
            }
        }

        public void resume() {
            if (!done) {
                synchronized(this) {
                    if (cmd == null) {
                        suspended = false;
                        return;
                    }
                }
                cmd.resume();
            }
        }

        public void setUpThrottling() {
            Buffers.getBuffers(BUFDIR).getThrottleManager().register(cb);
        }

        public void cancelThrottling() {
            ThrottleManager.getDefault(BUFDIR).unregister(cb);
        }
    }

    private static class CustomPutFileCmd extends PutFileCommand {
        private CReadBuffer buffer;
        private Writer handle;
        private boolean suspended;

        public CustomPutFileCmd(String local, String remote, long length, Writer handle) throws IOException,
                InterruptedException {
            super(local, remote, length);
            this.handle = handle;
        }

        public void resume() {
            synchronized(this) {
                suspended = false;
            }
            getChannel().sendTaggedData(getId(), CoasterChannel.SIGNAL_FLAG, PutFileHandler.CONTINUE);
        }

        public void suspend() {
            suspended = true;
            getChannel().sendTaggedData(getId(), CoasterChannel.SIGNAL_FLAG, PutFileHandler.STOP);
        }

        protected ReadBuffer createBuffer() throws FileNotFoundException, InterruptedException {
            return buffer = new CReadBuffer(Buffers.getBuffers(Direction.IN), this);
        }

        public CReadBuffer getBuffer() {
            return buffer;
        }
    }

    private static class CReadBuffer extends ReadBuffer {
        
        private int crt;
        private LinkedList<Buffers.Allocation> alloc;

        protected CReadBuffer(Buffers buffers, ReadBufferCallback cb) {
            super(buffers, cb, -1);
            alloc = new LinkedList<Buffers.Allocation>();
        }

        public void error(Exception e) {
            getCallback().error(true, e);
        }

        public void queue(boolean last, ByteBuffer buf) throws InterruptedException {
            if (logger.isDebugEnabled()) {
                logger.debug(getCallback() + " got data");
            }
            Buffers.Allocation a = buffers.request(1, this);
            synchronized(this) {
                crt++;
                alloc.add(a);
            }
            getCallback().dataRead(last, buf);
        }
        
                @Override
        public String getName() {
            return "POR-" + getCallback();
        }

        @Override
        public boolean isAlive() {
            return true;
        }

        public synchronized void freeFirst() {
            buffers.free(alloc.removeFirst());
            crt--;
            notify();
        }

        protected void deallocateBuffers() {
        }

        public void doStuff(boolean last, ByteBuffer b, Buffers.Allocation alloc) {
            // not used
        }
    }

    private static class Reader implements IOReader, Callback {
        private CustomGetFileCmd cmd;
        private ReadIOCallback cb;
        private CoasterChannel channel;
        private boolean done;
        private String src;

        public Reader(String src, String dst, ReadIOCallback cb) throws IOException {
            if (cb == null) {
                throw new NullPointerException();
            }
            this.cb = cb;
            this.src = src;
            RemoteFile uri = newRemoteFile(src);
            cmd = new CustomGetFileCmd("file://localhost/" + uri.getPath(), dst, this);
            channel = ChannelManager.getManager().getExistingChannel("id://" + uri.getHost(), null);
        }
        
        public String toString() {
            return "PR " + src;
        }

        public void start() throws IOException {
            try {
                logger.debug("Sending proxy get");
                cmd.executeAsync(channel, this);
                logger.debug("Proxy get sent");
                cb.info(String.valueOf(cmd.getId()));
            }
            catch (ProtocolException e) {
                logger.warn("Error requesting file from " + channel, e);
                throw new IOException("Error requesting file from " + channel);
            }
        }

        private RemoteFile newRemoteFile(String src) throws IOException {
            try {
                return new RemoteFile(src);
            }
            catch (Exception e) {
                throw new IOException("Invalid file name: " + e.getMessage());
            }
        }

        public boolean hasNext() {
            return !done;
        }

        public void sendNext() {
            // does nothing since all data is queued for sending
            // as soon as it is received
        }

        public void dataSent() {
            cmd.cwb.releaseOne();
        }

        public void errorReceived(Command cmd, String msg, Exception t) {
        	if (GetFileHandler.ABORTED.equals(msg)) {
        	    if (logger.isInfoEnabled()) {
        	        logger.info(this.cb + " client acknowledged abort");
        	    }
        	}
        	else {
        	    if (msg == null) {
        	        cb.error(this, t);
        	    }
        	    else {
        	        if (t == null) {
        	            cb.error(this, new IOException(msg));
        	        }
        	        else {
        	            cb.error(this, new IOException(msg, t));
        	        }
        	    }
        	}
        }

        public void replyReceived(Command cmd) {
            done = true;
        }

        public void close() {
        }

        public void abort() throws IOException {
            cmd.abort();
            close();
        }

        public void resume() {
            // not needed here
        }

        public void suspend() {
            // not needed here
        }
    }

    private static class CustomGetFileCmd extends GetFileCommand {
        private final ReadIOCallback cb;
        private final Reader handle;
        public CWriteBuffer cwb;
        private boolean suspended;

        public CustomGetFileCmd(String src, String dst, Reader handle) throws IOException {
            super(src, dst, null);
            if (handle == null) {
                throw new NullPointerException();
            }
            this.handle = handle;
            this.cb = handle.cb;
        }

        protected WriteBuffer createWriteBuffer() throws IOException {
            return cwb = new CWriteBuffer(Buffers.getBuffers(Direction.OUT), this);
        }

        protected void setLen(long len) {
            super.setLen(len);
            cb.length(len);
        }
        
            @Override
        public void handleSignal(byte[] data) {
            if (Arrays.equals(GetFileHandler.QUEUED.getBytes(), data)) {
                setQueued(true);
                cb.queued();
            }
        }

        public synchronized void abort() throws IOException {
            if (logger.isInfoEnabled()) {
                logger.info(this + " forwarding abort request");
            }
            cwb.close();
            // if the channel died, there is an unknown number of 
            // buffers that were queued to be sent on the channel 
            // but didn't make it through, so make sure things
            // get cleaned up
            cwb.releaseAll();
            getChannel().sendTaggedData(getId(), CoasterChannel.SIGNAL_FLAG, GetFileHandler.ABORT.getBytes());
        }

        public void resume() {
            if (cwb.isAlive() && suspended) {
                if (logger.isInfoEnabled()) {
                    logger.info(this + " resuming");
                }
                suspended = false;
                getChannel().sendTaggedData(getId(), CoasterChannel.SIGNAL_FLAG, GetFileHandler.RESUME.getBytes());
            }
        }

        public void suspend() {
            if (cwb.isAlive() && !suspended) {
                if (logger.isInfoEnabled()) {
                    logger.info(this + " suspending");
                }
                suspended = true;
                getChannel().sendTaggedData(getId(), CoasterChannel.SIGNAL_FLAG, GetFileHandler.SUSPEND.getBytes());
            }
        }
    }

    private static class CWriteBuffer extends WriteBuffer {
        private final CustomGetFileCmd cmd;
        public List<Allocation> alloc;
        private boolean closed;

        protected CWriteBuffer(Buffers buffers, CustomGetFileCmd cmd) {
            super(buffers);
            this.cmd = cmd;
            alloc = new LinkedList<Allocation>();
        }

        public void releaseAll() {
        	synchronized(alloc) {
            	for (Allocation a : alloc) {
            		buffers.free(a);
            	}
            	alloc.clear();
        	}
        }

        public void releaseOne() {
            Allocation a;
            boolean resume;
            synchronized(alloc) {
            	if (alloc.isEmpty()) {
            	    if (logger.isInfoEnabled()) {
            	        logger.info(cmd + " releaseOne. Spurious release.");
            	    }
            		return;
            	}
                a = alloc.remove(0);
                if (logger.isDebugEnabled()) {
                    logger.debug(cmd + " releaseOne. allocsz: " + alloc.size());
                }
                resume = alloc.size() == Buffers.ENTRIES_PER_STREAM / 2;
            }
            if (resume) {
                cmd.resume();
            }
            buffers.free(a);
        }

        public void doStuff(boolean last, ByteBuffer b, Buffers.Allocation alloc) {
            try {
                cmd.cb.data(cmd.handle, b, last);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        public void write(boolean last, byte[] data) throws InterruptedException {
        	if (closed) {
        	    return;
        	}
            Allocation a = buffers.request(1, this);
            boolean suspend;
            synchronized(alloc) {
                alloc.add(a);
                suspend = alloc.size() == Buffers.MAX_ENTRIES / 4;
            }
            if (suspend) {
                cmd.suspend();
            }
            buffers.queueRequest(last, ByteBuffer.wrap(data), this, this);
        }
        
        

        @Override
        public String getName() {
            return "POW-" + cmd;
        }

        @Override
        public boolean isAlive() {
            return !closed;
        }

        private byte[] getByteArray(ByteBuffer b) {
            if (b.hasArray()) {
                return b.array();
            }
            else {
                byte[] ba = new byte[b.limit()];
                b.get(ba);
                return ba;
            }
        }
        
        public void close() {
        	// don't free buffers here
        	// they get freed when the data is sent
       		closed = true;
        }
    }
}
