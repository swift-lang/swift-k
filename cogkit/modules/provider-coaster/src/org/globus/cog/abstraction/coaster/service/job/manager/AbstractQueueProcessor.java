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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.CoasterService;
import org.globus.cog.abstraction.coaster.service.LocalTCPService;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.coaster.channels.CoasterChannel;

public abstract class AbstractQueueProcessor extends Thread implements QueueProcessor {
    public static final Logger logger = Logger.getLogger(AbstractQueueProcessor.class);
    
    private final BlockingQueue<Job> q;
    private boolean shutdownFlag;
    private boolean wrap;
    private final LocalTCPService localService;
    private int queueSeq;
    private File script;

    public AbstractQueueProcessor(String name, LocalTCPService localService) {
        super(name);
        this.localService = localService;
        q = new LinkedBlockingQueue<Job>();
    }

    public LocalTCPService getLocalService() {
        return localService;
    }

    public void enqueue(Task t) {
        enqueue(new Job(t));
    }
    
    protected void enqueue(Job j) {
        if (shutdownFlag) {
            throw new IllegalStateException("Queue is shut down");
        }
        q.offer(j);
        queueSeq++;
    }

    public void startShutdown() {
        shutdownFlag = true;
    }

    protected boolean getShutdownFlag() {
        return shutdownFlag;
    }

    protected final Job take() throws InterruptedException {
        return q.take();
    }
    
    protected Queue<Job> getQueue() {
        return q;
    }
    
    protected Collection<Job> getAllJobs() {
        return new ArrayList<Job>(q);
    }
    
    protected Collection<Job> getRunningJobs() {
        throw new UnsupportedOperationException();
    }

    protected final boolean hasWrapped() {
        return wrap;
    }

    protected int queuedTaskCount() {
        return q.size();
    }

    @Override
    public synchronized void start() {
        try {
            script = ScriptManager.writeScript();
        }
        catch (Exception e) {
            CoasterService.error(19, "Cannot write worker script", e);
        }
        if (this.isAlive()) {
            throw new RuntimeException(this.getName() + " already started");
        }
        super.start();
    }
    
    public File getScript() {
        return script;
    }
    
    public int getQueueSeq() {
        return queueSeq;
    }

    @Override
    public void cancelTasksForChannel(CoasterChannel channel) {
        cancelTasksForChannel(channel, null);
    }
    
    @Override
    public void cancelTasksForChannel(CoasterChannel channel, String taskId) {
        String id = channel.getID();
        for (Job job : getAllJobs()) {
            if (job.getTask() == null) {
                continue;
            }
            if (!matchesTaskId(taskId, job)) {
                continue;
            }
            
            String taskChannelId = (String) job.getTask().getAttribute("channelId");
            if (id.equals(taskChannelId)) {
                job.cancel();
            }
        }
    }

    protected boolean matchesTaskId(String taskId, Job job) {
        if (taskId == null) {
            return true;
        }
        else {
            return job.getTask().getIdentity().toString().startsWith(taskId);
        }
    }
}
