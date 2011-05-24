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
import org.globus.cog.abstraction.coaster.service.LocalTCPService;
import org.globus.cog.abstraction.coaster.service.RegistrationManager;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;

public class JobQueue implements RegistrationManager {
    public static final Logger logger = Logger.getLogger(JobQueue.class);

    private QueueProcessor local, coaster;
    private final Settings settings;
    private final LocalTCPService localService;
    private ChannelContext clientChannelContext;
    private String defaultQueueProcessor;

    public JobQueue(LocalTCPService localService) {
        settings = new Settings();
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
        local = new LocalQueueProcessor();
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
            coaster.setClientChannelContext(clientChannelContext);
            coaster.start();
        }
        return coaster;
    }
    
    public void ensureQueueProcessorInitialized(String name) {
        getQueueProcessor(name);
    }

    private QueueProcessor newQueueProcessor(String name) {
        if (name.equals("local")) {
            return new LocalQueueProcessor();
        }
        else if (name.equals("block")) {
            return new BlockQueueProcessor(settings);
        }
        else if (name.equals("passive")) {
            return new PassiveQueueProcessor(settings, localService.getContact());
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

    public String nextId(String id) {
        return ((RegistrationManager) coaster).nextId(id);
    }

    public String registrationReceived(String blockID,
                                       String workerID,
                                       String workerHostname,
                                       ChannelContext channelContext) {
        RegistrationManager manager = ((RegistrationManager) coaster);
        return manager.registrationReceived(blockID, workerID,
                                            workerHostname,
                                            channelContext);
    }

    public QueueProcessor getCoasterQueueProcessor() {
        return coaster;
    }
}
