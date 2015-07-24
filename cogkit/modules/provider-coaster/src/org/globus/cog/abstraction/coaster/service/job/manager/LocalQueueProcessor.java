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

import org.globus.cog.abstraction.coaster.service.LocalTCPService;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.ExecutionTaskHandler;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

public class LocalQueueProcessor extends AbstractQueueProcessor {
    private TaskHandler taskHandler;
    private boolean done;

    public LocalQueueProcessor(LocalTCPService localService) {
        super("Local Queue Processor", localService);
        this.taskHandler = new ExecutionTaskHandler();
    }

    public void run() {
        try {
            Job at;
            while (!getShutdownFlag()) {
                at = take();
                Task t = at.getTask();
                if (t == null && getShutdownFlag()) {
                    break;
                }
                try {
                    t.setService(0, buildService(t));
                    taskHandler.submit(t);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    t.setStatus(new StatusImpl(Status.FAILED, null, e));
                }
            }
            done = true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startShutdown() {
        super.startShutdown();
        // wake up the loop
        getQueue().offer(new Job(null));
    }

    @Override
    public boolean isShutDown() {
        return done;
    }

    public void setBroadcaster(Broadcaster b) {
    }

    public static ExecutionService buildService(Task prototype)
            throws InvalidServiceContactException, InvalidProviderException,
            ProviderMethodException {
        ExecutionService s = new ExecutionServiceImpl();
        s.setServiceContact(prototype.getService(0).getServiceContact());
        ExecutionService p = (ExecutionService) prototype.getService(0);
        String jm = p.getJobManager();
        // this jm is provider and provider is jm and part of the jm is the
        // provider
        // while jm is inside the provider and the provider is part of the jm is
        // getting a bit confusing
        if (jm == null) {
            jm = "local";
        }
        int colon = jm.indexOf(':');
        // remove provider used to bootstrap coasters
        jm = jm.substring(colon + 1);
        colon = jm.indexOf(':');
        if (colon == -1) {
            s.setProvider(jm);
        }
        else {
            s.setJobManager(jm.substring(colon + 1));
            s.setProvider(jm.substring(0, colon));
        }
        if (p.getSecurityContext() != null) {
            s.setSecurityContext(p.getSecurityContext());
        }
        else {
            s.setSecurityContext(AbstractionFactory.getSecurityContext(s.getProvider(), s.getServiceContact()));
        }
        return s;
    }

    @Override
    public AbstractSettings getSettings() {
        return null;
    }
}
