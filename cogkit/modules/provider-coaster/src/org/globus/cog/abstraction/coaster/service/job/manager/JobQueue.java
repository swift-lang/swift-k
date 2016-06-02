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
 * Created on Feb 13, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.CoasterService;
import org.globus.cog.abstraction.coaster.service.LocalTCPService;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.coaster.channels.CoasterChannel;

public class JobQueue {
    public static final Logger logger = Logger.getLogger(JobQueue.class);

    private static int sid;
    private String id;
    private QueueProcessor local, coaster;
    private final LocalTCPService localService;
    private CoasterChannel clientChannel;
    private Broadcaster broadcaster;
    private String defaultQueueProcessor;
    private CoasterService service;

    public JobQueue(CoasterService service, LocalTCPService localService, CoasterChannel clientChannel) {
        synchronized(JobQueue.class) {
            id = String.valueOf(sid++);
        }
        this.service = service;
        this.localService = localService;
        this.broadcaster = new Broadcaster();
        if (clientChannel != null) {
            broadcaster.addChannel(clientChannel);
        }
        this.clientChannel = clientChannel;
    }

    public void start() {
        local = new LocalQueueProcessor(localService);
        local.setBroadcaster(broadcaster);
        local.start();
    }

    public void enqueue(Task t) {
        Service s = t.getService(0);
        JobSpecification spec = (JobSpecification) t.getSpecification();
        if (spec.isBatchJob()) {
            if (logger.isInfoEnabled()) {
                logger.info("Job batch mode flag set. Routing through local queue.");
            }
        }
        QueueProcessor qp;
        if (!spec.isBatchJob()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding task " + t + " to coaster queue");
            }
            qp = getQueueProcessor();
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
    
    public void initQueueProcessor(String name) {
        if (name == null) {
            name = "block";
        }
        coaster = newQueueProcessor(name);
        coaster.setBroadcaster(broadcaster);
        AbstractSettings settings = coaster.getSettings();
        Collection<URI> addrs = settings.getLocalContacts(localService.getPort());
        if (addrs == null) {
            settings.setCallbackURI(localService.getContact());
        }
        else {
            settings.setCallbackURIs(addrs);
        }
    }
    
    public void startQueueProcessor() {
        coaster.start();
    }

    public synchronized QueueProcessor getQueueProcessor() {
        if (coaster == null) {
            throw new IllegalArgumentException("No worker manager initialized");
        }
        return coaster;
    }
    
    private QueueProcessor newQueueProcessor(String name) {
        if (name.equals("local")) {
            return new LocalQueueProcessor(localService);
        }
        else if (name.equals("block")) {
            return new BlockQueueProcessor(localService);
        }
        else if (name.equals("passive")) {
            return new PassiveQueueProcessor(localService, localService.getContact());
        }
        else if (name.startsWith("class:")) {
            return newQueueProcessorFromClassName(name.substring(6));
        }
        else {
            throw new IllegalArgumentException("No such queue processor: " + name);
        }
    }
    
    private static final Class<?>[] BQ_CONS_PARAM_TYPES = new Class[] {LocalTCPService.class};

    private QueueProcessor newQueueProcessorFromClassName(String clsName) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends QueueProcessor> cls = (Class<? extends QueueProcessor>) Class.forName(clsName);
            Constructor<? extends QueueProcessor> cons = cls.getConstructor(BQ_CONS_PARAM_TYPES);
            return cons.newInstance(localService);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Error instantiating class for worker manager: '" + clsName + "'", e);
        }
    }

    public void startShutdown() {
        local.startShutdown();
        if (coaster != null) {
            coaster.startShutdown();
        }
    }
    
    public void waitForShutdown() {
        try {
            while (!local.isShutDown()) {
                Thread.sleep(100);
            }
            if (coaster != null) {
                while (!coaster.isShutDown()) {
                    Thread.sleep(100);
                }
            }
        }
        catch (InterruptedException e) {
            logger.info("Interrupted", e);
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
    
    public String toString() {
        return "JobQueue" + id;
    }

    public CoasterChannel getClientChannel() {
        return clientChannel;
    }

    public Broadcaster getBroadcaster() {
        return broadcaster;
    }
    
    public void cancelTasksForChannel(CoasterChannel channel) {
        local.cancelTasksForChannel(channel);
        coaster.cancelTasksForChannel(channel);
    }
    
    public void cancelTasksForChannel(CoasterChannel channel, String taskId) {
        local.cancelTasksForChannel(channel, taskId);
        coaster.cancelTasksForChannel(channel, taskId);
    }
}
