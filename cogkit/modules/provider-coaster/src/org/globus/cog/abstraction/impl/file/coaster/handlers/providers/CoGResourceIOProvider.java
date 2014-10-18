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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

public class CoGResourceIOProvider implements IOProvider {
    public static final Logger logger = Logger.getLogger(CoGResourceIOProvider.class);

    public void abort(IOHandle handle) throws IOException {
        ((Abortable) handle).abort();
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

    private static class Writer implements IOWriter, WriteBufferCallback, Abortable {
        private static final Direction BUFDIR = Direction.OUT;
        
        private File f;
        private long len, crt;
        private WriteIOCallback cb;
        private WriteBuffer buf;

        public Writer(String dest, WriteIOCallback cb) throws IOException {
            this.cb = cb;
            f = CoasterFileRequestHandler.normalize(dest);
        }

        public void close() throws IOException {
            buf.close();
        }

        public void setLength(long len) throws IOException {
            this.len = len;
            if (len == -1) {
                buf = Buffers.newDeleteFileWriteBuffer(Buffers.getBuffers(BUFDIR), f, this);
            }
            else {
                File p = f.getParentFile();
                if (!p.exists()) {
                    if (!p.mkdirs()) {
                        throw new IOException("Failed to create directory " + p.getAbsolutePath());
                    }
                } 
                if (len == 0) {
                	buf = Buffers.newEmptyFileWriteBuffer(Buffers.getBuffers(BUFDIR), f, this);
                }
                else {
                    buf = Buffers.newWriteBuffer(Buffers.getBuffers(BUFDIR), f, this);
                }
            }
        }

        public void write(boolean last, byte[] data) throws IOException {
            try {
                buf.write(last, data);
                crt += data.length;
                if (last && crt != len) {
                    throw new IOException("File size mismatch. Expected " + len + " bytes, got "
                            + crt);
                }
            }
            catch (InterruptedException e) {
                throw new IOException(e.toString());
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
            buf.close();
            f.delete();
        }

        /**
         * Used to notify upstream handler that the transfer
         * has been suspended and that what otherwise would be
         * timeouts are benign
         */
        public void suspend() {
        }

        /**
         * The opposite of suspend()
         */
        public void resume() {
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
            f = CoasterFileRequestHandler.normalize(src);
            this.cb = cb;
            fc = new FileInputStream(f).getChannel();
        }

        public String toString() {
            return "LR " + f;
        }

        public void start() throws IOException {
            cb.length(f.length());
            try {
                synchronized (this) {
                    rbuf = Buffers.newReadBuffer(Buffers.getBuffers(Direction.IN), fc, f.length(), this);
                }
            }
            catch (InterruptedException e) {
                throw new IOException(e.toString());
            }
        }

        public synchronized void dataSent() {
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
            try {
                fc.close();
            }
            catch (IOException e) {
                logger.warn("Failed to close file channel", e);
            }
        }

        public void abort() throws IOException {
            close();
        }
        
        public void resume() {
            // TODO
        }

        public void suspend() {
            // TODO
        }
    }
}
