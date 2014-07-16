package org.globus.cog.abstraction.impl.execution.gt2;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.gram.GramJob;
import org.globus.gram.GramJobListener;

public class MultiJobListener implements GramJobListener {
    static Logger logger = Logger.getLogger(MultiJobListener.class.getName());
    private int runningJobs = 0;
    private Task task = null;
    private boolean failed = false;

    public MultiJobListener(Task task) {
        this.task = task;
    }

    public void runningJob() {
        runningJobs++;
    }

    public void failed(boolean value) {
        this.failed = value;
    }

    public synchronized void done() {
        runningJobs--;
        if (runningJobs <= 0 && !failed) {
            this.task.setStatus(Status.COMPLETED);
        }
    }

    public void statusChanged(GramJob job) {
        if (job.getStatus() == GramJob.STATUS_DONE) {
            done();
        } else if (job.getStatus() == GramJob.STATUS_FAILED) {
            this.failed = true;
            Status status = new StatusImpl();
            status.setPrevStatusCode(this.task.getStatus().getStatusCode());
            status.setStatusCode(Status.FAILED);
            status.setMessage("ErrorCode: " + job.getError());
            this.task.setStatus(status);
        } else if (job.getStatus() == GramJob.STATUS_ACTIVE && !this.failed) {
            this.task.setStatus(Status.ACTIVE);
        }
    }
}