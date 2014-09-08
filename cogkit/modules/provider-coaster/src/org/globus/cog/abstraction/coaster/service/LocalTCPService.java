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
 * Created on Feb 15, 2008
 */
package org.globus.cog.abstraction.coaster.service;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.job.manager.Block;
import org.globus.cog.abstraction.coaster.service.job.manager.BlockQueueProcessor;
import org.globus.cog.coaster.ConnectionHandler;
import org.globus.cog.coaster.RequestManager;
import org.globus.cog.coaster.Service;
import org.globus.cog.coaster.ServiceContext;
import org.globus.cog.coaster.channels.ChannelException;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.channels.TCPChannel;
import org.globus.common.CoGProperties;
import org.globus.net.PortRange;

public class LocalTCPService implements Registering, Service, Runnable {
    public static final Logger logger = Logger.getLogger(LocalTCPService.class);

    public static final int TCP_BUFSZ = 32768;

    private final TCPBufferManager buffMan;

    private ServerSocketChannel channel;
    private int port;
    
    private RequestManager requestManager;
    private ServiceContext context = new ServiceContext(this);
    
    private Thread serverThread;
    
    private URI contact;
    private BlockRegistry blockRegistry;
    private Map<String, CoasterChannel> workerChannels;

    public LocalTCPService(RequestManager rm) throws IOException {
        this(rm, 0);
    }

    public LocalTCPService(RequestManager rm, int port) throws IOException {
        setRequestManager(rm);
        buffMan = new TCPBufferManager();
        this.port = port;
        this.blockRegistry = new BlockRegistry();
    }

    public String registrationReceived(String blockid, String url, 
           CoasterChannel channel, Map<String, String> options) throws ChannelException {
        if (logger.isInfoEnabled()) {
            logger.info("Received registration: blockid = " +
                        blockid + ", url = " + url);
        }
        String wid = blockRegistry.nextId(blockid);
        channel.setName(blockid + ":" + wid);
        blockRegistry.registrationReceived(blockid, wid, url, channel, options);
        return wid;
    }
    
    public void registerBlock(Block block, BlockQueueProcessor bqp) {
        blockRegistry.addBlock(block.getId(), bqp);
    }
    
    public void unregisterBlock(Block block) {
        blockRegistry.removeBlock(block.getId());
    }
    
    public BlockQueueProcessor getQueueProcessor(String blockId) {
        return blockRegistry.getQueueProcessor(blockId);
    }

    public void unregister(String id) {
        throw new UnsupportedOperationException();
    }
    
    protected void setRequestManager(RequestManager requestManager) {
        this.requestManager = requestManager;
    }
    
    public ServiceContext getContext() {
        return context;
    }
    
    public void start() {
        try {
            channel = ServerSocketChannel.open();
            channel.configureBlocking(true);
            if (port == 0) {
                PortRange portRange = PortRange.getTcpInstance();
                if (portRange != null && portRange.isEnabled()) {
                    while (true) {
                        synchronized(portRange) {
                            port = portRange.getFreePort(port);
                            portRange.setUsed(port);
                        }
                        /*
                         *  the jglobus port range only parses the cog configuration
                         *  options, but does not check if a port is actually in use
                         *  or not, so try to bind the port and if that fails, increment
                         *  and continue. If the jglobus tcp range runs out of ports,
                         *  it will throw an I/O exception.
                         */
                        
                        if (bindPort()) {
                            break;
                        }
                        port++;
                    }
                }
                else {
                    /*
                     *  if no port is specified and no port range is defined,
                     *  let the TCP implementation pick
                     */
                    channel.socket().bind(null);
                    port = channel.socket().getLocalPort();
                }
            }
            else {
                bindPort();
            }
            
            if (serverThread == null) {
                serverThread = new Thread(this);
                serverThread.setDaemon(true);
                serverThread.setName("Local TCP service");
                serverThread.start();
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public boolean bindPort() throws IOException {
        try {
            channel.socket().bind(new InetSocketAddress(port));
            return true;
        }
        catch (BindException e) {
            return false;
        }
    }
    
    public void run() {
        while(true) {
            try {
                SocketChannel c = channel.accept();
                handleConnection(c.socket());
            }
            catch (IOException e) {
                logger.warn("Accept() failed", e);
                return;
            }
        }
    }

    protected void handleConnection(Socket socket) {
        try {
            buffMan.addSocket(socket);
            socket.setSoLinger(false, 0);
            socket.setTcpNoDelay(true);
        }
        catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            new WorkerConnectionHandler(this, buffMan.wrap(socket), requestManager).start();
        }
        catch (Exception e) {
            logger.warn("Could not start worker connection", e);
        }
    }
    
    public synchronized URI getContact() {
        if (contact == null) {
            String host = CoGProperties.getDefault().getIPAddress();
            if (host == null) {
                try {
                    host = InetAddress.getLocalHost().getHostAddress();
                }
                catch (UnknownHostException e) {
                    host = "127.0.0.1";
                }
            }
            try {
                contact = new URI("http://" + host + ":" + getPort());
            }
            catch (URISyntaxException e) {
                logger.warn("Cannot build local service contact URI", e);
            }
        }
        return contact;
    }
    
    public int getPort() {
        if (port == 0) {
            return channel.socket().getLocalPort();
        }
        else {
            return port;
        }
    }

    public boolean isRestricted() {
        return false;
    }

    public void irrecoverableChannelError(CoasterChannel channel, Exception e) {
        System.err.println("Irrecoverable channel exception: " + e.getMessage());
        System.exit(2);
    }
    
    private static int idSeq = 1;
    
    private synchronized static int nextId() {
        return idSeq++;
    }

    private static class WorkerConnectionHandler extends ConnectionHandler {
        public WorkerConnectionHandler(Service service, Socket socket, RequestManager requestManager)
                throws IOException {
            super(socket, new WorkerChannel(socket, requestManager, service), requestManager);
        }
    }
    
    private static class WorkerChannel extends TCPChannel {
        public WorkerChannel(Socket socket, RequestManager requestManager, Service service) throws IOException {
            super(socket, requestManager, null);
            setName("worker-" + nextId());
            setService(service);
        }

        @Override
        protected boolean clientControlsHeartbeats() {
            // Reverse the heartbeat direction for coaster workers because it makes detection of lost
            // connections quicker (at least when heartbeats are considered lost if not seen for
            // x > interval).
            return false;
        }
    }    
}
