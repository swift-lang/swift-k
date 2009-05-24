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
import java.net.Socket;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.GSSService;
import org.globus.cog.karajan.workflow.service.RequestManager;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.ChannelException;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;

public class LocalTCPService extends GSSService implements Registering {
    public static final Logger logger = Logger.getLogger(LocalTCPService.class);
    
    private RegistrationManager registrationManager;
    
    private int idseq;
    
    private static final NumberFormat IDF = new DecimalFormat("000000");

    public LocalTCPService(RequestManager rm) throws IOException {
        super(false, 0);
        setRequestManager(rm);
    }

    public String registrationReceived(String blockid, String url, KarajanChannel channel) throws ChannelException {
        if (logger.isInfoEnabled()) {
            logger.info("Received registration: blockid = " + blockid + ", url = " + url);
        }
        ChannelContext cc = channel.getChannelContext();
        cc.getChannelID().setLocalID(blockid);
        String wid = registrationManager.nextId(blockid);
        cc.getChannelID().setRemoteID(wid);
        ChannelManager.getManager().registerChannel(cc.getChannelID(), channel);
        registrationManager.registrationReceived(blockid, wid, channel.getChannelContext());
        return wid;
    }

    public RegistrationManager getRegistrationManager() {
        return registrationManager;
    }

    public void setRegistrationManager(RegistrationManager workerManager) {
        this.registrationManager = workerManager;
    }

    protected void handleConnection(Socket socket) {
        try {
            socket.setReceiveBufferSize(2048);
            socket.setSendBufferSize(4096);
            socket.setTcpNoDelay(true);
        }
        catch (SocketException e) {
            e.printStackTrace();
        }
        super.handleConnection(socket);
    }
}
