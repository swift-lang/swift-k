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
 * Created on Jan 19, 2008
 */
package org.globus.cog.abstraction.coaster.service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.rlog.RemoteLogCommand;
import org.globus.cog.abstraction.coaster.rlog.RemoteLogHandler;
import org.globus.cog.abstraction.coaster.service.job.manager.JobQueue;
import org.globus.cog.abstraction.coaster.service.job.manager.PassiveQueueProcessor;
import org.globus.cog.abstraction.coaster.service.job.manager.QueueProcessor;
import org.globus.cog.abstraction.coaster.service.local.JobStatusHandler;
import org.globus.cog.abstraction.coaster.service.local.RegistrationHandler;
import org.globus.cog.abstraction.impl.execution.coaster.NotificationManager;
import org.globus.cog.abstraction.impl.execution.coaster.ServiceManager;
import org.globus.cog.abstraction.impl.execution.coaster.bootstrap.Bootstrap;
import org.globus.cog.abstraction.impl.file.coaster.handlers.GetFileHandler;
import org.globus.cog.abstraction.impl.file.coaster.handlers.PutFileHandler;
import org.globus.cog.coaster.ConnectionHandler;
import org.globus.cog.coaster.GSSService;
import org.globus.cog.coaster.RemoteConfiguration;
import org.globus.cog.coaster.RequestManager;
import org.globus.cog.coaster.ServiceRequestManager;
import org.globus.cog.coaster.channels.ChannelContext;
import org.globus.cog.coaster.channels.ChannelException;
import org.globus.cog.coaster.channels.ChannelManager;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.channels.PipedClientChannel;
import org.globus.cog.coaster.channels.PipedServerChannel;
import org.globus.gsi.gssapi.auth.SelfAuthorization;
import org.ietf.jgss.GSSCredential;

public class CoasterService extends GSSService {
    public static final Logger logger = Logger
            .getLogger(CoasterService.class);

    public static final int IDLE_TIMEOUT = 120 * 1000;

    public static final int CONNECT_TIMEOUT = 2 * 60 * 1000;

    public static final RequestManager COASTER_REQUEST_MANAGER = new CoasterRequestManager();

    private String registrationURL, id, defaultQP;
    private Map<String, JobQueue> queues;
    private LocalTCPService localService;
    private Exception exceptionAtStop;
    private boolean done;
    private boolean suspended;
    private static Timer watchdogs = new Timer();
    private boolean local;
    private CoasterChannel channelToClient;
    private boolean ignoreIdleTime;
    private PassiveQueueProcessor passiveQP;

    public CoasterService() throws IOException {
        this(true);
    }

    public CoasterService(boolean local) throws IOException {
        this(null, null, local);
    }

    public CoasterService(boolean secure, int port, InetAddress bindTo) throws IOException {
        super(secure, port, bindTo);
        queues = new HashMap<String, JobQueue>();
        setPollingIntervals();
    }

    public CoasterService(GSSCredential cred, int port, InetAddress bindTo) throws IOException {
        super(cred, port, bindTo);
        queues = new HashMap<String, JobQueue>();
        setPollingIntervals();
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
        queues = new HashMap<String, JobQueue>();
        setAuthorization(new SelfAuthorization());
        initializeLocalService();
        setPollingIntervals();
    }

    private void setPollingIntervals() {
        setPollingInterval("pbs", 15);
        setPollingInterval("slurm", 15);
        setPollingInterval("lsf", 15);
        setPollingInterval("sge", 15);
    }

    private void setPollingInterval(String p, int t) {
        try {
            String clsName = "org.globus.cog.abstraction.impl.scheduler." + p + ".Properties";
            Class<?> cls = CoasterService.class.getClassLoader().loadClass(clsName);
            Method getProperties = cls.getMethod("getProperties", (Class<?>[]) null);
            Object instance = getProperties.invoke(null, (Object[]) null);
            Method setPollInterval = cls.getMethod("setPollInterval", new Class<?>[] {int.class});
            setPollInterval.invoke(instance, new Object[] {t});
        }
        catch (Exception e) {
            logger.warn("Failed to set polling interval for " + p, e);
        }
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
                ConnectionHandler handler = new ConnectionHandler("cps-" + sock.getPort(), this, sock,
                        COASTER_REQUEST_MANAGER);
                handler.start();
            }
            catch (Exception e) {
                logger.warn("Could not start connection handler", e);
            }
        }
    }

    public JobQueue createJobQueue() {
        JobQueue q = new JobQueue(this, localService);
        q.start();
        if (defaultQP != null) {
            q.ensureQueueProcessorInitialized(defaultQP);
        }
        addJobQueue(q);
        return q;
    }

    @Override
    public void start() {
        super.start();
        try {
            if (localService == null) {
                throw new IllegalStateException("Local service not initialized");
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

    private CoasterChannel createLocalChannel() throws IOException, ChannelException {
        PipedServerChannel psc =
                ServiceManager.getDefault().getLocalService().newPipedServerChannel();
        PipedClientChannel pcc =
                new PipedClientChannel(COASTER_REQUEST_MANAGER, new ChannelContext("cpipe"), psc);
        psc.setClientChannel(pcc);
        ChannelManager.getManager().registerChannel(pcc.getChannelContext().getChannelID(), pcc);
        ChannelManager.getManager().registerChannel(psc.getChannelContext().getChannelID(), psc);
        return pcc;
    }

    private void stop(Exception exception) {
        for (JobQueue q : queues.values()) {
            q.shutdown();
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
                logMemoryUsage();
            }
            if (exceptionAtStop != null) {
                throw exceptionAtStop;
            }
        }
    }

    private void logMemoryUsage() {
        if (!local) {
            if (logger.isInfoEnabled()) {
                Runtime r = Runtime.getRuntime();
                long maxHeap = r.maxMemory();
                long freeMemory = r.freeMemory();
                long totalMemory = r.totalMemory();
                long usedMemory = totalMemory - freeMemory;

                logger.info("HeapMax: " + maxHeap + ", CrtHeap: " + totalMemory + ", UsedHeap: " + usedMemory);
            }
        }
    }

    @Override
    public void irrecoverableChannelError(CoasterChannel channel, Exception e) {
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
            for (JobQueue q : queues.values()) {
                q.shutdown();
            }
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

    protected void addJobQueue(JobQueue q) {
        synchronized(queues) {
            queues.put(q.getId(), q);
        }
    }

    public JobQueue getJobQueue(String id) {
        synchronized(queues) {
            return queues.get(id);
        }
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
                "KEEPALIVE, RECONNECT(8), HEARTBEAT(30)");
    }

    public static void main(String[] args) {
        try {
            configureLogName();
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

    public static void configureLogName() {
        FileAppender fa = (FileAppender) getFileAppender();
        if (fa != null) {
            fa.setFile(Bootstrap.LOG_DIR + File.separator + makeLogFileName());
            fa.activateOptions();

            AsyncAppender aa = new AsyncAppender();
            aa.addAppender(fa);
            replaceAppender(fa, aa);
        }
    }

    private static String makeLogFileName() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return "coasters-" + df.format(new Date()) + ".log";
    }

    private static void replaceAppender(FileAppender fa, AsyncAppender aa) {
        Logger root = Logger.getRootLogger();
        root.removeAppender(fa);
        root.addAppender(aa);
    }

    @SuppressWarnings("rawtypes")
    protected static Appender getFileAppender() {
        Logger root = Logger.getRootLogger();
        Enumeration e = root.getAllAppenders();
        while (e.hasMoreElements()) {
            Appender a = (Appender) e.nextElement();
            if (a instanceof FileAppender) {
                return a;
            }
            if (a instanceof AsyncAppender) {
                // likely this is running in a JVM in which
                // the file appender has been replaced with
                // an async appender, so don't mess with things
                return null;
            }
        }
        logger.warn("Could not find a file appender to configure");
        return null;
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

    public Map<String, JobQueue> getQueues() {
        return queues;
    }

    public synchronized QueueProcessor getPassiveQueueProcessor() {
        if (passiveQP == null) {
            passiveQP = new PassiveQueueProcessor(localService, localService.getContact());
        }
        return passiveQP;
    }
}
