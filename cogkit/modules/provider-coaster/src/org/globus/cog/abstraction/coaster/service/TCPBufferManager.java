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
 * Created on Mar 9, 2011
 */
package org.globus.cog.abstraction.coaster.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

public class TCPBufferManager {
    public static final Logger logger = Logger.getLogger(TCPBufferManager.class);
    
    public static final int K = 1024;
    public static final int M = K*K;
    public static final int MAX_COMBINED_BUFFER_SIZES = 32 * M;
    public static final int MIN_COMBINED_BUFFER_SIZES = 1 * M;
    public static final int MIN_BUFFER_SIZE = 16 * K;
    public static final int BUFFER_SIZE_GRANULARITY = 1 * K;
    
    private final Set<Socket> sockets;
    private int crtSocketBuffSz;
    
    public TCPBufferManager() {
        sockets = new HashSet<Socket>();
        crtSocketBuffSz = (MIN_COMBINED_BUFFER_SIZES + MAX_COMBINED_BUFFER_SIZES) / 2;
    }
    
    public void addSocket(Socket socket) throws SocketException {
        synchronized (sockets) {
            checkClosed();
            sockets.add(socket);
            setBuffersToCurrentSize(socket);
            checkSizes();
        }
    }

    public void removeSocket(Socket socket) throws SocketException {
        synchronized (sockets) {
            checkClosed();
            sockets.remove(socket);
            checkSizes();
        }
    }
    
    private void checkClosed() {
        Iterator<Socket> i = sockets.iterator();
        while (i.hasNext()) {
            if (i.next().isClosed()) {
                i.remove();
            }
        }
    }

    private void setBuffersToCurrentSize(Socket socket) throws SocketException {
        socket.setReceiveBufferSize(crtSocketBuffSz);
        socket.setSendBufferSize(crtSocketBuffSz);
    }
    
    private void checkSizes() throws SocketException {
        int max = MAX_COMBINED_BUFFER_SIZES / BUFFER_SIZE_GRANULARITY;
        int min = MIN_COMBINED_BUFFER_SIZES / BUFFER_SIZE_GRANULARITY;
        int mid = (max + min) / 2;
        int crt = crtSocketBuffSz / BUFFER_SIZE_GRANULARITY;
        int old = crt;
        
        if (logger.isDebugEnabled()) {
            logger.debug("crt: " + crt + ", #sockets: " + sockets.size() + ", min: " + min + ", max: " + max);
        }
        if (sockets.size() == 0) {
            logger.debug("No sockets");
            return;
        }
        if (crt * sockets.size() > max || crt * sockets.size() < min) {
            crt = mid / sockets.size();
            if (crt != old) {
                crtSocketBuffSz = crt * BUFFER_SIZE_GRANULARITY;
                if (crtSocketBuffSz < MIN_BUFFER_SIZE) {
                    crtSocketBuffSz = MIN_BUFFER_SIZE;
                }
                if (logger.isInfoEnabled()) {
                    logger.info("Adjusting buffer size to " + crtSocketBuffSz + " for " + sockets.size() + " sockets");
                }
                updateBufferSizes();
            }
        }
    }

    private void updateBufferSizes() throws SocketException {
        for (Socket s : sockets) {
            setBuffersToCurrentSize(s);
        }
    }
    
    public Socket wrap(Socket s) {
        return new SocketWrapper(s);
    }
    
    private final class SocketWrapper extends Socket {
        private final Socket s;
        
        public SocketWrapper(Socket delegate) {
            this.s = delegate;
        }
        
        public int hashCode() {
            return s.hashCode();
        }

        public boolean equals(Object obj) {
            return s.equals(obj);
        }

        public void connect(SocketAddress endpoint) throws IOException {
            s.connect(endpoint);
        }

        public void connect(SocketAddress endpoint, int timeout) throws IOException {
            s.connect(endpoint, timeout);
        }

        public void bind(SocketAddress bindpoint) throws IOException {
            s.bind(bindpoint);
        }

        public InetAddress getInetAddress() {
            return s.getInetAddress();
        }

        public InetAddress getLocalAddress() {
            return s.getLocalAddress();
        }

        public int getPort() {
            return s.getPort();
        }

        public int getLocalPort() {
            return s.getLocalPort();
        }

        public SocketAddress getRemoteSocketAddress() {
            return s.getRemoteSocketAddress();
        }

        public SocketAddress getLocalSocketAddress() {
            return s.getLocalSocketAddress();
        }

        public SocketChannel getChannel() {
            return s.getChannel();
        }

        public InputStream getInputStream() throws IOException {
            return s.getInputStream();
        }

        public OutputStream getOutputStream() throws IOException {
            return s.getOutputStream();
        }

        public void setTcpNoDelay(boolean on) throws SocketException {
            s.setTcpNoDelay(on);
        }

        public boolean getTcpNoDelay() throws SocketException {
            return s.getTcpNoDelay();
        }

        public void setSoLinger(boolean on, int linger) throws SocketException {
            s.setSoLinger(on, linger);
        }

        public int getSoLinger() throws SocketException {
            return s.getSoLinger();
        }

        public void sendUrgentData(int data) throws IOException {
            s.sendUrgentData(data);
        }

        public void setOOBInline(boolean on) throws SocketException {
            s.setOOBInline(on);
        }

        public boolean getOOBInline() throws SocketException {
            return s.getOOBInline();
        }

        public void setSoTimeout(int timeout) throws SocketException {
            s.setSoTimeout(timeout);
        }

        public int getSoTimeout() throws SocketException {
            return s.getSoTimeout();
        }

        public void setSendBufferSize(int size) throws SocketException {
            s.setSendBufferSize(size);
        }

        public int getSendBufferSize() throws SocketException {
            return s.getSendBufferSize();
        }

        public void setReceiveBufferSize(int size) throws SocketException {
            s.setReceiveBufferSize(size);
        }

        public int getReceiveBufferSize() throws SocketException {
            return s.getReceiveBufferSize();
        }

        public void setKeepAlive(boolean on) throws SocketException {
            s.setKeepAlive(on);
        }

        public boolean getKeepAlive() throws SocketException {
            return s.getKeepAlive();
        }

        public void setTrafficClass(int tc) throws SocketException {
            s.setTrafficClass(tc);
        }

        public int getTrafficClass() throws SocketException {
            return s.getTrafficClass();
        }

        public void setReuseAddress(boolean on) throws SocketException {
            s.setReuseAddress(on);
        }

        public boolean getReuseAddress() throws SocketException {
            return s.getReuseAddress();
        }

        public void shutdownInput() throws IOException {
            s.shutdownInput();
        }

        public void shutdownOutput() throws IOException {
            s.shutdownOutput();
        }

        public String toString() {
            return s.toString();
        }

        public boolean isConnected() {
            return s.isConnected();
        }

        public boolean isBound() {
            return s.isBound();
        }

        public boolean isClosed() {
            return s.isClosed();
        }

        public boolean isInputShutdown() {
            return s.isInputShutdown();
        }

        public boolean isOutputShutdown() {
            return s.isOutputShutdown();
        }

        public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
            s.setPerformancePreferences(connectionTime, latency, bandwidth);
        }

        public synchronized void close() throws IOException {
            removeSocket(this);
            s.close();
        }
    }
}
