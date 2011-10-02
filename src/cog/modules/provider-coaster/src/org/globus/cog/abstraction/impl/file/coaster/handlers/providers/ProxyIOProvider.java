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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.file.coaster.buffers.Buffers;
import org.globus.cog.abstraction.impl.file.coaster.buffers.ReadBuffer;
import org.globus.cog.abstraction.impl.file.coaster.buffers.ReadBufferCallback;
import org.globus.cog.abstraction.impl.file.coaster.buffers.WriteBuffer;
import org.globus.cog.abstraction.impl.file.coaster.commands.GetFileCommand;
import org.globus.cog.abstraction.impl.file.coaster.commands.PutFileCommand;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.channels.ChannelException;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.commands.Command.Callback;

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
        private CustomPutFileCmd cmd;
        private WriteIOCallback cb;
        private KarajanChannel channel;
        private String src, dst;

        public Writer(String src, String dst, WriteIOCallback cb) throws IOException {
            this.cb = cb;
            this.src = src;
            this.dst = dst;
        }
        
        public String toString() {
            return "PW " + src + " -> " + dst;
        }

        public void close() throws IOException {
            if (channel != null) {
                ChannelManager.getManager().releaseChannel(channel);
            }
        }

        public void setLength(long len) throws IOException {
            try {
                URI uri = new URI(dst);
                cmd = new CustomPutFileCmd(src, "file://localhost/" + uri.getPath().substring(1), len, this);
                channel = ChannelManager.getManager().reserveChannel("id://" + uri.getHost(), null);
                cmd.executeAsync(channel, this);
            }
            catch (Exception e) {
                throw new IOException(e.getMessage());
            }
        }

        public void write(boolean last, byte[] data) throws IOException {
            try {
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
            ChannelManager.getManager().releaseChannel(channel);
        }

        public void abort() throws IOException {
            close();
        }
    }

    private static class CustomPutFileCmd extends PutFileCommand {
        private CReadBuffer buffer;
        private Writer handle;

        public CustomPutFileCmd(String local, String remote, long length, Writer handle) throws IOException,
                InterruptedException {
            super(local, remote, length);
            this.handle = handle;
        }

        protected ReadBuffer createBuffer() throws FileNotFoundException, InterruptedException {
            return buffer = new CReadBuffer(Buffers.getDefault(), this);
        }

        public CReadBuffer getBuffer() {
            return buffer;
        }

        public void replyReceived(boolean fin, boolean err, byte[] data) throws ProtocolException {
            if (err) {
                handle.cb.error(handle, new Exception(new String(data)));
            }
            else {
                handle.cb.done(handle);
            }
        }
    }

    private static class CReadBuffer extends ReadBuffer {
        // private Exception error;
        // private BlockingQueue queue;
        // private boolean seenLast;
        private int crt;
        private LinkedList<Buffers.Allocation> alloc;

        protected CReadBuffer(Buffers buffers, ReadBufferCallback cb) {
            super(buffers, cb, -1);
            // queue = new LinkedBlockingQueue();
            alloc = new LinkedList<Buffers.Allocation>();
        }

        public void error(Exception e) {
            getCallback().error(true, e);
        }

        public void queue(boolean last, ByteBuffer buf) throws InterruptedException {
            if (logger.isDebugEnabled()) {
                logger.debug(getCallback() + " got data");
            }
            Buffers.Allocation a = buffers.request(1);
            synchronized(this) {
                while (crt >= Buffers.ENTRIES_PER_STREAM) {
                    logger.debug("Reached max stream buffers. Waiting");
                    wait();
                }
                crt++;
                alloc.add(a);
                /* if (last) {
                    seenLast = true;
                } */
            }
            getCallback().dataRead(last, buf);
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

    /*
    private static class Entry {
        public final boolean last;
        public final ByteBuffer buf;

        public Entry(boolean last, ByteBuffer buf) {
            this.last = last;
            this.buf = buf;
        }
    }
    */

    private static class Reader implements IOReader, Callback {
        private CustomGetFileCmd cmd;
        private ReadIOCallback cb;
        private KarajanChannel channel;
        private boolean done;
        private String src;

        public Reader(String src, String dst, ReadIOCallback cb) throws IOException {
            if (cb == null) {
                throw new NullPointerException();
            }
            this.cb = cb;
            this.src = src;
            URI uri = newURI(src);
            cmd = new CustomGetFileCmd("file://localhost/" + uri.getPath().substring(1), dst, this);
            try {
                channel = ChannelManager.getManager().reserveChannel("id://" + uri.getHost(), null);
            }
            catch (ChannelException e) {
                throw new IOException("Cannot establish channel to " + uri.getHost());
            }
        }
        
        public String toString() {
            return "PR " + src;
        }

        public void start() throws IOException {
            try {
                logger.debug("Sending proxy get");
                cmd.executeAsync(channel, this);
                logger.debug("Proxy get sent");
            }
            catch (ProtocolException e) {
                logger.warn("Error requesting file from " + channel, e);
                throw new IOException("Error requesting file from " + channel);
            }
        }

        private URI newURI(String src) throws IOException {
            try {
                return new URI(src);
            }
            catch (URISyntaxException e) {
                throw new IOException("Malformed URI: " + e.getMessage());
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
        }

        public void errorReceived(Command cmd, String msg, Exception t) {
            cb.error(this, t);
        }

        public void replyReceived(Command cmd) {
            done = true;
        }

        public void close() {
            if (channel != null) {
                ChannelManager.getManager().releaseChannel(channel);
            }
        }

        public void abort() throws IOException {
            close();
        }
    }

    private static class CustomGetFileCmd extends GetFileCommand {
        private final ReadIOCallback cb;
        private final Reader handle;

        public CustomGetFileCmd(String src, String dst, Reader handle) throws IOException {
            super(src, dst, null);
            if (handle == null) {
                throw new NullPointerException();
            }
            this.handle = handle;
            this.cb = handle.cb;
        }

        protected WriteBuffer createWriteBuffer() throws IOException {
            return new CWriteBuffer(Buffers.getDefault(), this);
        }

        protected void setLen(long len) {
            super.setLen(len);
            cb.length(len);
        }
    }

    private static class CWriteBuffer extends WriteBuffer {
        private final CustomGetFileCmd cmd;

        protected CWriteBuffer(Buffers buffers, CustomGetFileCmd cmd) {
            super(buffers);
            this.cmd = cmd;
        }

        public void doStuff(boolean last, ByteBuffer b, Buffers.Allocation alloc) {
            try {
                cmd.cb.data(cmd.handle, b, last);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
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
    }
}
