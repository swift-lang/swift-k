//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 30, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.ExecutionTaskHandler;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.TaskHandler;

class BlockTaskSubmitter extends Thread {
    public static final Logger logger = Logger.getLogger(BlockTaskSubmitter.class);

    private final LinkedList<Block> queue;
    private final TaskHandler handler;

    public BlockTaskSubmitter() {
        setDaemon(true);
        setName("Block Submitter");
        queue = new LinkedList<Block>();
        handler = new ExecutionTaskHandler();
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
        handler.cancel(block.getTask());
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
                        handler.submit(b.getTask());
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
}
