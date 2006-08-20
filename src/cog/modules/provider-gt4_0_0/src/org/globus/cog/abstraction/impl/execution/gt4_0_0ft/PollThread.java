//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.gt4_0_0ft;

import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.exec.client.GramJob;


public class PollThread implements Runnable {

    private Task task = null;
    private GramJob gramJob = null;
    
    public PollThread(Task task, GramJob gramJob){
    this.task = task;    
    this.gramJob = gramJob;
    }
    
    public void run() {
        int status = this.task.getStatus().getStatusCode();
        while(status != Status.COMPLETED && status != Status.FAILED){
            try {
                this.gramJob.refreshStatus();
                Thread.sleep(5000);
            } catch (Exception e) {
               // do nothing .... continue
            }
            
        }
    }

}
