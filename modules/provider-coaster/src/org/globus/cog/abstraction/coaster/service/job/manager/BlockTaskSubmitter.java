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
import org.globus.cog.abstraction.interfaces.TaskHandler;

class BlockTaskSubmitter extends Thread {
    public static final Logger logger = Logger.getLogger(BlockTaskSubmitter.class);
    
    private LinkedList queue;
    private TaskHandler handler;
    
    public BlockTaskSubmitter() {
        setDaemon(true);
        setName("Block Submitter");
        queue = new LinkedList();
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
    
    public void cancel(Block block) throws InvalidSecurityContextException, TaskSubmissionException {
        handler.cancel(block.getTask());
    }
    
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
                b = (Block) queue.removeFirst();
            }
            if (b != null) {
                if (logger.isInfoEnabled()) {
                    logger.info("Submitting block " + b);
                }
                try {
                    handler.submit(b.getTask());
                }
                catch (Exception e) {
                	if (logger.isInfoEnabled()) {
                		logger.info("Error submitting block task", e);
                	}
                    b.taskFailed("Error submitting block task", e);
                }
            }
        }
    }
}