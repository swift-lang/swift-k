//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 1, 2010
 */
package org.globus.cog.abstraction.impl.file.coaster.handlers.providers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.file.coaster.buffers.Buffers;
import org.globus.cog.abstraction.impl.file.coaster.buffers.Buffers.Direction;
import org.globus.cog.abstraction.impl.file.coaster.buffers.ReadBuffer;
import org.globus.cog.abstraction.impl.file.coaster.buffers.ReadBufferCallback;
import org.globus.cog.abstraction.impl.file.coaster.buffers.ThrottleManager;
import org.globus.cog.abstraction.impl.file.coaster.buffers.WriteBuffer;
import org.globus.cog.abstraction.impl.file.coaster.buffers.WriteBufferCallback;
import org.globus.cog.abstraction.impl.file.coaster.handlers.CoasterFileRequestHandler;
import org.globus.cog.abstraction.interfaces.RemoteFile;

public class LocalIOProvider implements IOProvider {
    public static final Logger logger = Logger.getLogger(LocalIOProvider.class); 

    public void abort(IOHandle handle) throws IOException {
        if (handle != null) {
            ((Abortable) handle).abort();
        }
    }

    public IOReader pull(String src, String dest, ReadIOCallback cb) throws IOException {
        return new Reader(src, cb);
    }

    public IOWriter push(String src, String dest, WriteIOCallback cb) throws IOException {
        return new Writer(dest, cb);
    }

    public boolean isDirect() {
        return true;
    }
    
    private static RemoteFile newRemoteFile(String src) throws IOException {
        try {
            return new RemoteFile(src);
        }
        catch (Exception e) {
            throw new IOException("Invalid name: " + e.getMessage());
        }
    }

    private static class Writer implements IOWriter, WriteBufferCallback, Abortable {
        private static Direction BUFDIR = Direction.OUT;
        
        private File f;
        private long len, crt;
        private WriteIOCallback cb;
        private WriteBuffer buf;

        public Writer(String dest, WriteIOCallback cb) throws IOException {
            this.cb = cb;
            RemoteFile destURI = newRemoteFile(dest);
            f = CoasterFileRequestHandler.normalize(destURI);
        }
        
        public String toString() {
            return "LW " + f;
        }

        public void close() throws IOException {
            buf.close();
        }

        public void setLength(long len) throws IOException {
            this.len = len;
            if (len == 0) {
                // no further writes
                buf = Buffers.newEmptyFileWriteBuffer(Buffers.getBuffers(Direction.OUT), f, this);
            }
            else {
                buf = Buffers.newWriteBuffer(Buffers.getBuffers(Direction.OUT), f, this);
            }
        }

        public void write(boolean last, byte[] data) throws IOException {
            crt += data.length;
            if (last && crt != len) {
                throw new IOException(" File size mismatch. Expected " + len + " bytes, got "
                        + crt);
            }
            try {
                buf.write(last, data);
            }
            catch (InterruptedException e) {
                throw new IOException("Interrupted!");
            }
        }

        public void done(boolean last) {
            if (last) {
                cb.done(this);
            }
        }

        public void error(boolean last, Exception e) {
            cb.error(this, e);
        }

        public void abort() throws IOException {
            if (buf != null) {
                buf.close();
            }
            f.delete();
        }

        public void suspend() {
            // not used
        }

        public void resume() {
            // not used
        }
        
        public void setUpThrottling() {
            Buffers.getBuffers(BUFDIR).getThrottleManager().register(cb);
        }

        public void cancelThrottling() {
            ThrottleManager.getDefault(BUFDIR).unregister(cb);
        }
    }

    private static class Reader implements IOReader, ReadBufferCallback {
        private File f;
        private ReadIOCallback cb;
        private ReadBuffer rbuf;
        private FileChannel fc;

        public Reader(String src, ReadIOCallback cb) throws IOException {
            if (logger.isDebugEnabled()) {
                logger.debug("LocalIOProvider.Reader " + src);
            }
            RemoteFile srcURI = newRemoteFile(src);
            f = CoasterFileRequestHandler.normalize(srcURI);
            this.cb = cb;
            fc = new FileInputStream(f).getChannel();
        }
        
        public String toString() {
            return "LR-" + cb;
        }

        public void start() throws IOException {
            cb.length(f.length());
            try {
                synchronized(this) {
                    rbuf = Buffers.newReadBuffer(Buffers.getBuffers(Direction.IN), fc, f.length(), this);
                }
                if (logger.isInfoEnabled()) {
                    logger.info(this + " rbuf: " + rbuf);
                }
            }
            catch (InterruptedException e) {
                throw new IOException(e.toString());
            }
        }

        public synchronized void dataSent() {
            logger.debug("Data sent");
            rbuf.freeFirst();
        }

        public void dataRead(boolean last, ByteBuffer buf) {
            cb.data(this, buf, last);
            if (last) {
                close();
            }
        }

        public void queued() {
            cb.queued();
        }

        private synchronized void closeBuffer() {
            try {
                rbuf.close();
            }
            catch (IOException e) {
                logger.warn("Failed to close read buffer", e);
            }
        }

        public void error(boolean last, Exception e) {
            cb.error(this, e);
            close();
        }

        public void close() {
            closeBuffer();
        }

        public void abort() throws IOException {
            close();
        }

        public void resume() {
        	rbuf.resume();
        }

        public void suspend() {
        	rbuf.suspend();
        }
    }
}
