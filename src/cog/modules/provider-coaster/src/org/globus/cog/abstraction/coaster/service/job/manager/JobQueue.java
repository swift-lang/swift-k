//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 13, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.net.URI;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.CoasterService;
import org.globus.cog.abstraction.coaster.service.LocalTCPService;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.coaster.channels.ChannelContext;

public class JobQueue {
    public static final Logger logger = Logger.getLogger(JobQueue.class);

    private static int sid;
    private String id;
    private QueueProcessor local, coaster;
    private final Settings settings;
    private final LocalTCPService localService;
    private ChannelContext clientChannelContext;
    private String defaultQueueProcessor;
    private CoasterService service;

    public JobQueue(CoasterService service, LocalTCPService localService) {
        synchronized(JobQueue.class) {
            id = String.valueOf(sid++);
        }
        settings = new Settings();
        this.service = service;
        this.localService = localService;
        Collection<URI> addrs = settings.getLocalContacts(localService.getPort());
        if (addrs == null) {
            settings.setCallbackURI(localService.getContact());
        }
        else {
            settings.setCallbackURIs(addrs);
        }
    }

    public void start() {
        local = new LocalQueueProcessor(localService);
        local.start();
    }

    public void enqueue(Task t) {
        Service s = t.getService(0);
        // String jm = null;
        JobSpecification spec = (JobSpecification) t.getSpecification();
        // if (s instanceof ExecutionService) {
        //    jm = ((ExecutionService) s).getJobManager();
        // }
        if (spec.isBatchJob()) {
            if (logger.isInfoEnabled()) {
                logger.info("Job batch mode flag set. Routing through local queue.");
            }
        }
        QueueProcessor qp;
        if (s.getProvider().equalsIgnoreCase("coaster") && !spec.isBatchJob()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding task " + t + " to coaster queue");
            }
            qp = getQueueProcessor(settings.getWorkerManager());
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding task " + t + " to local queue");
            }
            qp = local;
        }
        qp.enqueue(t);
    }
    
    public synchronized void setQueueProcessor(QueueProcessor qp) {
        coaster = qp;
    }

    public synchronized QueueProcessor getQueueProcessor(String name) {
        if (coaster == null) {
            coaster = newQueueProcessor(name);
            if (clientChannelContext != null) {
                coaster.setClientChannelContext(clientChannelContext);
            }
            coaster.start();
        }
        return coaster;
    }
    
    public void ensureQueueProcessorInitialized(String name) {
        getQueueProcessor(name);
    }

    private QueueProcessor newQueueProcessor(String name) {
        if (name.equals("local")) {
            return new LocalQueueProcessor(localService);
        }
        else if (name.equals("block")) {
            return new BlockQueueProcessor(localService, settings);
        }
        else if (name.equals("passive")) {
            return service.getPassiveQueueProcessor();
        }
        else {
            throw new IllegalArgumentException("No such queue processor: " + name);
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public void shutdown() {
        local.shutdown();
        if (coaster != null)
            coaster.shutdown();
    }

    public void setClientChannelContext(ChannelContext channelContext) {
        if (this.clientChannelContext != null) {
            return;
        }
        this.clientChannelContext = channelContext;
        local.setClientChannelContext(channelContext);
        if (coaster != null) {
            coaster.setClientChannelContext(channelContext);
        }
    }

    public QueueProcessor getCoasterQueueProcessor() {
        return coaster;
    }

    public LocalTCPService getLocalService() {
        return localService;
    }
    
    public String getId() {
        return id;
    }
}
