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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.ConnectionHandler;
import org.globus.cog.karajan.workflow.service.RequestManager;
import org.globus.cog.karajan.workflow.service.Service;
import org.globus.cog.karajan.workflow.service.ServiceContext;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.ChannelException;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.channels.TCPChannel;

public class LocalTCPService implements Registering, Service, Runnable {
    public static final Logger logger = Logger.getLogger(LocalTCPService.class);

    public static final int TCP_BUFSZ = 32768;

    private RegistrationManager registrationManager;
    private final TCPBufferManager buffMan;

    private ServerSocketChannel channel;
    private int port;
    
    private RequestManager requestManager;
    private ServiceContext context = new ServiceContext(this);
    
    private Thread serverThread;

    public LocalTCPService(RequestManager rm) throws IOException {
        this(rm, 0);
    }

    public LocalTCPService(RequestManager rm, int port) throws IOException {
        setRequestManager(rm);
        buffMan = new TCPBufferManager();
        this.port = port;
    }

    public String registrationReceived(String blockid, String url, 
           KarajanChannel channel, Map<String, String> options) throws ChannelException {
        if (logger.isInfoEnabled()) {
            logger.info("Received registration: blockid = " +
                        blockid + ", url = " + url);
        }
        ChannelContext cc = channel.getChannelContext();
        cc.getChannelID().setLocalID(blockid);
        String wid = registrationManager.nextId(blockid);
        cc.getChannelID().setRemoteID(wid);
        ChannelManager.getManager().registerChannel(cc.getChannelID(), channel);
        registrationManager.registrationReceived(blockid, wid, url, cc, options);
        return wid;
    }

    public void unregister(String id) {
        throw new UnsupportedOperationException();
    }

    public RegistrationManager getRegistrationManager() {
        return registrationManager;
    }

    public void setRegistrationManager(RegistrationManager workerManager) {
        this.registrationManager = workerManager;
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
            channel.socket().bind(new InetSocketAddress(port));
            
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
    
    public URI getContact() {
        return null;
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

    public void irrecoverableChannelError(KarajanChannel channel, Exception e) {
        System.err.println("Irrecoverable channel exception: " + e.getMessage());
        System.exit(2);
    }

    private static class WorkerConnectionHandler extends ConnectionHandler {
        public WorkerConnectionHandler(Service service, Socket socket, RequestManager requestManager)
                throws IOException {
            super(socket, new WorkerChannel(socket, requestManager, 
                new ChannelContext(service)), requestManager);
        }
    }
    
    private static class WorkerChannel extends TCPChannel {
        public WorkerChannel(Socket socket, RequestManager requestManager,
                ChannelContext channelContext) throws IOException {
            super(socket, requestManager, channelContext);
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
