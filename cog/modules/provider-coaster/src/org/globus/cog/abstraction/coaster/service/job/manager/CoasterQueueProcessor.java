//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 13, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.util.Queue;

public class CoasterQueueProcessor extends QueueProcessor {
    public static final Logger logger = Logger
            .getLogger(CoasterQueueProcessor.class);
    private TaskHandler taskHandler;
    private WorkerManager workerManager;
    private String workdir;

    public CoasterQueueProcessor(WorkerManager workerManager)
            throws IOException {
        super("Coaster Queue Processor");
        this.workerManager = workerManager;
        this.taskHandler = new CoasterTaskHandler(workerManager);
    }

    public void run() {
        try {
            new Thread() {
                {
                    setDaemon(true);
                }

                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(20000);
                            Queue q = getQueue();
                            logger.info("Coaster queue: " + q);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();

            workerManager.start();
            AssociatedTask at;
            while (!this.getShutdownFlag()) {
                at = next();
                Worker wr = workerManager.request(at.maxWallTime, at.task);
                if (wr != null) {
                    remove();
                    if (wr.getStatus() != null) {
                        at.task.setStatus(wr.getStatus());
                    }
                    else {
                        try {
                            at.task.getService(0).setServiceContact(
                                    new ServiceContactImpl(wr.getId()));
                            new WorkerTaskMonitor(at.task, workerManager, wr);
                            taskHandler.submit(at.task);
                        }
                        catch (Exception e) {
                            at.task.setStatus(new StatusImpl(Status.FAILED,
                                    null, e));
                        }
                    }
                }
                if (hasWrapped()) {
                    synchronized (workerManager) {
                        workerManager.wait(1000);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
