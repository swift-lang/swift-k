//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 19, 2008
 */
package org.globus.cog.abstraction.coaster.service;

import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.job.manager.JobQueue;
import org.globus.cog.abstraction.coaster.service.local.JobStatusHandler;
import org.globus.cog.abstraction.coaster.service.local.RegistrationHandler;
import org.globus.cog.abstraction.impl.execution.coaster.NotificationManager;
import org.globus.cog.karajan.workflow.service.ConnectionHandler;
import org.globus.cog.karajan.workflow.service.GSSService;
import org.globus.cog.karajan.workflow.service.RequestManager;
import org.globus.cog.karajan.workflow.service.ServiceRequestManager;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.gsi.gssapi.auth.SelfAuthorization;

public class CoasterService extends GSSService {
    public static final Logger logger = Logger
            .getLogger(CoasterService.class);

    public static final int IDLE_TIMEOUT = 600 * 1000;

    private String registrationURL, id;
    private JobQueue jobQueue;
    private LocalTCPService localService;
    private Exception e;
    private boolean done;
    private boolean suspended;

    public CoasterService() throws IOException {
        this(null, null);
    }

    public CoasterService(String registrationURL, String id)
            throws IOException {
        super();
        this.registrationURL = registrationURL;
        this.id = id;
        setAuthorization(new SelfAuthorization());
        RequestManager rm = new ServiceRequestManager();
        rm.addHandler("REGISTER", RegistrationHandler.class);
        rm.addHandler("JOBSTATUS", JobStatusHandler.class);
        localService = new LocalTCPService(rm);
    }

    protected void handleConnection(Socket sock) {
        logger.debug("Got connection");
        if (isSuspended()) {
            try {
                sock.close();
            }
            catch (IOException e) {
                logger.warn("Failed to close new connection", e);
            }
        }
        else {
            try {
                ConnectionHandler handler = new ConnectionHandler(this, sock,
                        new CoasterRequestManager());
                handler.start();
            }
            catch (Exception e) {
                logger.warn("Could not start connection handler", e);
            }
        }
    }

    public void start() {
        super.start();
        try {
            localService.start();
            jobQueue = new JobQueue(localService);
            jobQueue.start();
            localService.setWorkerManager(jobQueue.getWorkerManager());
            logger
                    .info("Started local service: "
                            + localService.getContact());
            if (id != null) {
                try {
                    logger.info("Reserving channel for registration");
                    KarajanChannel channel = ChannelManager.getManager()
                            .reserveChannel(registrationURL, null);
                    logger.info("Sending registration");
                    RegistrationCommand reg = new RegistrationCommand(id,
                            "https://" + getHost() + ":" + getPort());
                    reg.execute(channel);
                    logger.info("Registration complete");
                }
                catch (Exception e) {
                    throw new RuntimeException("Failed to register service",
                            e);
                }
            }
            logger.info("Started coaster service: " + this);
        }
        catch (Exception e) {
            logger.error("Failed to start coaster service", e);
            stop(e);
        }
    }

    private void stop(Exception e) {
        synchronized (this) {
            this.e = e;
            done = true;
            notifyAll();
        }
    }

    public void waitFor() throws Exception {
        synchronized (this) {
            while (!done) {
                wait(1000);
                checkIdleTime();
            }
            if (e != null) {
                throw e;
            }
        }
    }

    private synchronized void checkIdleTime() {
        // the notification manager should probably not be a singleton
        long idleTime = NotificationManager.getDefault().getIdleTime();
        if (idleTime > IDLE_TIMEOUT) {
            suspend();
            if (NotificationManager.getDefault().getIdleTime() < IDLE_TIMEOUT) {
                resume();
            }
            else {
                logger.info("Idle time exceeded. Shutting down service.");
                shutdown();
            }
        }
    }

    public synchronized void suspend() {
        this.suspended = true;
    }
    
    public synchronized boolean isSuspended() {
        return suspended;
    }
    
    public synchronized void resume() {
        this.suspended = false;
    }
    
    public void shutdown() {
        super.shutdown();
        jobQueue.getWorkerManager().shutdown();
        done = true;
    }

    public JobQueue getJobQueue() {
        return jobQueue;
    }

    public static void main(String[] args) {
        try {
            CoasterService s;
            if (args.length < 2) {
                s = new CoasterService();
            }
            else {
                s = new CoasterService(args[0], args[1]);
            }
            s.start();
            s.waitFor();
            System.exit(0);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
