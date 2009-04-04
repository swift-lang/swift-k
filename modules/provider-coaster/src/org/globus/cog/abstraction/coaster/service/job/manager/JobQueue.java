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
import org.globus.cog.abstraction.coaster.service.LocalTCPService;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Task;

public class JobQueue {
	public static final Logger logger = Logger.getLogger(JobQueue.class);
	
    private QueueProcessor local, coaster;
    private WorkerManager workerManager;
    
    public JobQueue(LocalTCPService localService) throws IOException {
        local = new LocalQueueProcessor();
        workerManager = new WorkerManager(localService);
        coaster = new CoasterQueueProcessor(workerManager);
    }
    
    public void start() {
        local.start();
        coaster.start();
    }

    public void enqueue(Task t) {
        Service s = t.getService(0);
        String jm = null;
        JobSpecification spec = (JobSpecification) t.getSpecification();
        if (s instanceof ExecutionService) {
            jm = ((ExecutionService) s).getJobManager();
        }
        if (spec.isBatchJob()) {
        	if (logger.isInfoEnabled()) {
        		logger.info("Job batch mode flag set. Routing through local queue.");
        	}
        }
        if (s.getProvider().equalsIgnoreCase("coaster") && !spec.isBatchJob()) {
            coaster.enqueue(t);
        }
        else {
            local.enqueue(t);
        }
    }
    
    public WorkerManager getWorkerManager() {
        return workerManager;
    }
}
