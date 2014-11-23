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
 * Created on Apr 30, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

class BlockTaskSubmitter extends Thread {
    public static final Logger logger = Logger.getLogger(BlockTaskSubmitter.class);

    private final LinkedList<Block> queue;
    private final Map<String, TaskHandler> handlers;

    public BlockTaskSubmitter() {
        setDaemon(true);
        setName("Block Submitter");
        queue = new LinkedList<Block>();
        handlers = new HashMap<String, TaskHandler>();
    }

    public void submit(Block block) {
        if (logger.isInfoEnabled()) {
            logger.info("Queuing block " + block + " for submission");
        }
        synchronized(queue) {
            queue.add(block);
            queue.notify();
        }
    }

    public void cancel(Block block)
            throws InvalidSecurityContextException, TaskSubmissionException {
        try {
            Task t = block.getTask();
            getHandler(getProvider(t)).cancel(t);
        }
        catch (InvalidProviderException e) {
            throw new TaskSubmissionException(e);
        }
        catch (ProviderMethodException e) {
            throw new TaskSubmissionException(e);
        }
    }

    private String getProvider(Task t) {
        return t.getService(0).getProvider();
    }

    @Override
    public void run() {
        while(true) {
            Block b = null;
            synchronized(queue) {
                while (queue.isEmpty()) {
                    try {
                        queue.wait();
                    }
                    catch (InterruptedException e) {
                        logger.warn("Interrupted");
                    }
                }
                b = queue.removeFirst();
            }
            if (b != null) {
                if (logger.isInfoEnabled()) {
                    logger.info("Submitting block " + b);
                }
                try {
                    if (!b.isShutDown()) {
                        getHandler(b.getTask().getService(0).getProvider()).submit(b.getTask());
                    }
                }
                catch (TaskSubmissionException e) {
                    if (b.getTask().getStatus().getStatusCode() != Status.CANCELED) {
                        logger.error("\n" + e.getMessage().trim());
                        b.taskFailed("Error submitting block task", e);
                    }
                    else {
                        logger.info("Block task was canceled previously: " + b);
                    }
                }
                catch (Exception e) {
                    if (b.getTask().getStatus().getStatusCode() != Status.CANCELED) {
                    	if (logger.isInfoEnabled()) {
                    		logger.info("Error submitting block task", e);
                    	}
                        b.taskFailed("Error submitting block task", e);
                    }
                    else {
                        if (logger.isInfoEnabled()) {
                            logger.debug("Block task was canceled previously " + b);
                        }
                    }
                }
            }
        }
    }

    public TaskHandler getHandler(String provider) throws InvalidProviderException, ProviderMethodException {
        synchronized (handlers) {
            TaskHandler h = handlers.get(provider);
            if (h == null) {
                h = AbstractionFactory.newExecutionTaskHandler(provider);
                handlers.put(provider, h);
            }
            return h;
        }
    }
}
