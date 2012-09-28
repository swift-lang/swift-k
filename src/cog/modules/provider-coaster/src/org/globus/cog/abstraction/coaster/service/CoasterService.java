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
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.rlog.RemoteLogCommand;
import org.globus.cog.abstraction.coaster.rlog.RemoteLogHandler;
import org.globus.cog.abstraction.coaster.service.job.manager.JobQueue;
import org.globus.cog.abstraction.coaster.service.local.JobStatusHandler;
import org.globus.cog.abstraction.coaster.service.local.RegistrationHandler;
import org.globus.cog.abstraction.impl.execution.coaster.NotificationManager;
import org.globus.cog.abstraction.impl.execution.coaster.ServiceManager;
import org.globus.cog.abstraction.impl.file.coaster.handlers.GetFileHandler;
import org.globus.cog.abstraction.impl.file.coaster.handlers.PutFileHandler;
import org.globus.cog.karajan.workflow.service.ConnectionHandler;
import org.globus.cog.karajan.workflow.service.GSSService;
import org.globus.cog.karajan.workflow.service.RemoteConfiguration;
import org.globus.cog.karajan.workflow.service.RequestManager;
import org.globus.cog.karajan.workflow.service.ServiceRequestManager;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.ChannelException;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.channels.PipedClientChannel;
import org.globus.cog.karajan.workflow.service.channels.PipedServerChannel;
import org.globus.gsi.gssapi.auth.SelfAuthorization;
import org.ietf.jgss.GSSCredential;

public class CoasterService extends GSSService {
    public static final Logger logger = Logger
            .getLogger(CoasterService.class);

    public static final int IDLE_TIMEOUT = 120 * 1000;

    public static final int CONNECT_TIMEOUT = 2 * 60 * 1000;

    public static final RequestManager COASTER_REQUEST_MANAGER = new CoasterRequestManager();

    private String registrationURL, id, defaultQP;
    private JobQueue jobQueue;
    private LocalTCPService localService;
    private Exception exceptionAtStop;
    private boolean done;
    private boolean suspended;
    private static Timer watchdogs = new Timer();
    private boolean local;
    private KarajanChannel channelToClient;
    private boolean ignoreIdleTime;

    public CoasterService() throws IOException {
        this(true);
    }

    public CoasterService(boolean local) throws IOException {
        this(null, null, local);
    }

    public CoasterService(boolean secure, int port, InetAddress bindTo) throws IOException {
        super(secure, port, bindTo);
    }

    public CoasterService(GSSCredential cred, int port, InetAddress bindTo) throws IOException {
        super(cred, port, bindTo);
    }

    public CoasterService(String registrationURL, String id, boolean local)
            throws IOException {
        super(!local, 0);
        this.local = local;
        if (local) {
            this.ignoreIdleTime = true;
        }
        addLocalHook();
        this.registrationURL = registrationURL;
        this.id = id;
        setAuthorization(new SelfAuthorization());
        initializeLocalService();
    }

    private RequestManager newLocalRequestManager() {
        RequestManager rm = new ServiceRequestManager();
        rm.addHandler("REGISTER", RegistrationHandler.class);
        rm.addHandler("JOBSTATUS", JobStatusHandler.class);
        rm.addHandler("GET", GetFileHandler.class);
        rm.addHandler("PUT", PutFileHandler.class);
        rm.addHandler(RemoteLogCommand.NAME, RemoteLogHandler.class);
        return rm;
    }

    protected void initializeLocalService() throws IOException {
        localService = new LocalTCPService(newLocalRequestManager());
        localService.start();
    }

    protected void initializeLocalService(int port) throws IOException {
        localService = new LocalTCPService(newLocalRequestManager(), port);
        localService.start();
    }


    @Override
    protected void handleConnection(Socket sock) {
        if (local) {
            logger.warn("Discarding connection");
            return;
        }
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
                        COASTER_REQUEST_MANAGER);
                handler.start();
            }
            catch (Exception e) {
                logger.warn("Could not start connection handler", e);
            }
        }
    }

    @Override
    public void start() {
        super.start();
        try {
            if (localService == null) {
                throw new IllegalStateException("Local service not initialized");
            }
            jobQueue = new JobQueue(localService);
            jobQueue.start();
            localService.setRegistrationManager(jobQueue);
            if (defaultQP != null) {
                jobQueue.ensureQueueProcessorInitialized(defaultQP);
            }
            logger.info("Started local service: "
                        + localService.getContact());
            if (id != null) {
                try {
                    logger.info("Reserving channel for registration");
                    RemoteConfiguration.getDefault().prepend(
                            getChannelConfiguration(registrationURL));

                    if (local) {
                        channelToClient = createLocalChannel();
                    }
                    else {
                        channelToClient = ChannelManager.getManager()
                            .reserveChannel(registrationURL, null,
                                    COASTER_REQUEST_MANAGER);
                    }
                    channelToClient.getChannelContext().setService(this);

                    logger.info("Sending registration");
                    String url = "https://" + getHost() + ":" + getPort();
                    RegistrationCommand reg = new RegistrationCommand(id, url);
                    reg.execute(channelToClient);
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
            System.err.println("Failed to start coaster service");
            e.printStackTrace();
            stop(e);
        }
    }

    private KarajanChannel createLocalChannel() throws IOException, ChannelException {
        PipedServerChannel psc =
                ServiceManager.getDefault().getLocalService().newPipedServerChannel();
        PipedClientChannel pcc =
                new PipedClientChannel(COASTER_REQUEST_MANAGER, new ChannelContext(), psc);
        psc.setClientChannel(pcc);
        ChannelManager.getManager().registerChannel(pcc.getChannelContext().getChannelID(), pcc);
        ChannelManager.getManager().registerChannel(psc.getChannelContext().getChannelID(), psc);
        return pcc;
    }

    private void stop(Exception exception) {
        if (jobQueue != null) {
            jobQueue.shutdown();
        }
        synchronized (this) {
            this.exceptionAtStop = exception;
            done = true;
            notifyAll();
        }
        unregister();
    }

    public void waitFor() throws Exception {
        synchronized (this) {
            while (!done) {
                wait(10000);
                checkIdleTime();
            }
            if (exceptionAtStop != null) {
                throw exceptionAtStop;
            }
        }
    }

    @Override
    public void irrecoverableChannelError(KarajanChannel channel, Exception e) {
        logger.warn("irrecoverable channel error!\n\t" + e);
        stop(e);
    }

    private synchronized void checkIdleTime() {
        if (ignoreIdleTime) {
            return;
        }
        // the notification manager should probably not be a singleton
        long idleTime = NotificationManager.getDefault().getIdleTime();
        logger.info("Idle time: " + idleTime);
        if (idleTime > IDLE_TIMEOUT) {
            suspend();
            if (NotificationManager.getDefault().getIdleTime() < IDLE_TIMEOUT) {
                resume();
            }
            else {
                logger.warn("Idle time exceeded. Shutting down service.");
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

    @Override
    public void shutdown() {
        try {
            startShutdownWatchdog();
            unregister();
            super.shutdown();
            jobQueue.shutdown();
        }
        finally {
            done = true;
        }
        logger.info("Shutdown sequence completed");
    }

    /**
     * This is needed if the service is running in local mode. The (client)
     * application may call System.exit before all the cleanup is done. This is
     * fine in non-local mode, but not otherwise.
     */
    public void addLocalHook() {
        if (local) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        while (!done) {
                            Thread.sleep(50);
                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void unregister() {
        try {
            if (id != null && channelToClient != null) {
                UnregisterCommand ur = new UnregisterCommand(id);
                ur.executeAsync(channelToClient);
            }
        }
        catch (Exception e) {
            logger.warn("Failed to unregister", e);
        }
    }

    private void startShutdownWatchdog() {
        if (!local) {
            watchdogs.schedule(new TimerTask() {
                @Override
                public void run() {
                    logger
                            .warn("Shutdown failed after 5 minutes. Forcefully shutting down");
                    System.exit(3);
                }
            }, 5 * 60 * 1000);
        }
    }

    private static TimerTask startConnectWatchdog() {
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                error(4, "Failed to connect after 2 minutes. Shutting down", null);
            }
        };
        watchdogs.schedule(tt, CONNECT_TIMEOUT);
        return tt;
    }

    public static void addWatchdog(TimerTask w, long delay) {
        synchronized (watchdogs) {
            watchdogs.schedule(w, delay);
        }
    }

    public static void addPeriodicWatchdog(TimerTask w, long delay) {
        synchronized (watchdogs) {
            watchdogs.schedule(w, delay, delay);
        }
    }

    public JobQueue getJobQueue() {
        return jobQueue;
    }

    public boolean getIgnoreIdleTime() {
        return ignoreIdleTime;
    }

    public void setIgnoreIdleTime(boolean ignoreIdleTime) {
        this.ignoreIdleTime = ignoreIdleTime;
    }

    protected RemoteConfiguration.Entry getChannelConfiguration(String contact) {
        return new RemoteConfiguration.Entry(
                contact.replaceAll("\\.", "\\."),
                "KEEPALIVE, RECONNECT(8), HEARTBEAT(300)");
    }

    public static void main(String[] args) {
        try {
            CoasterService s;
            boolean local = false;
            if (args.length < 2) {
                s = new CoasterService();
            }
            else {
                if (args.length == 3) {
                    local = "-local".equals(args[2]);
                }
                s = new CoasterService(args[0], args[1], local);
            }
            TimerTask t = startConnectWatchdog();
            s.start();
            t.cancel();
            s.waitFor();
            if (!local) {
                System.exit(0);
            }
        }
        catch (Exception e) {
            error(1, null, e);
        }
        catch (Throwable t) {
            error(2, null, t);
        }
    }

    public static void error(int code, String msg) {
        error(code, msg, null);
    }

    /**
       This reports a fatal error
     */
    public static void error(int code, String msg, Throwable t) {
        if (msg != null) {
        	System.err.println("CoasterService fatal error:");
            System.err.println(msg);
        }
        if (t != null) {
            t.printStackTrace();
        }
        logger.fatal(msg, t);
        System.exit(code);
    }

    public boolean isLocal() {
        return local;
    }

    protected LocalTCPService getLocalService() {
        return localService;
    }

    public String getDefaultQP() {
        return defaultQP;
    }

    public void setDefaultQP(String defaultQP) {
        this.defaultQP = defaultQP;
    }


}
