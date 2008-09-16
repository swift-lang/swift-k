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

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.job.manager.WorkerManager;
import org.globus.cog.karajan.workflow.service.GSSService;
import org.globus.cog.karajan.workflow.service.RequestManager;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.ChannelException;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;

public class LocalTCPService extends GSSService implements Registering {
    public static final Logger logger = Logger.getLogger(LocalTCPService.class);
    
    private WorkerManager workerManager;

    public LocalTCPService(RequestManager rm) throws IOException {
        super(false, 0);
        setRequestManager(rm);
    }

    public void registrationReceived(String id, String url, KarajanChannel channel) throws ChannelException {
        if (logger.isInfoEnabled()) {
            logger.info("Received registration: id = " + id + ", url = " + url);
        }
        ChannelContext cc = channel.getChannelContext();
        cc.getChannelID().setLocalID("coaster");
        cc.getChannelID().setRemoteID(id);
        ChannelManager.getManager().registerChannel(cc.getChannelID(), channel);
        workerManager.registrationReceived(id, url, channel.getChannelContext());
    }

    public WorkerManager getWorkerManager() {
        return workerManager;
    }

    public void setWorkerManager(WorkerManager workerManager) {
        this.workerManager = workerManager;
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
