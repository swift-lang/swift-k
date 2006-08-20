package org.globus.cog.abstraction.impl.common.task;

import org.globus.cog.abstraction.interfaces.Task;

public class JobSubmissionTask extends TaskImpl {
    public JobSubmissionTask() {
        super();
        setType(Task.JOB_SUBMISSION);
        setRequiredService(1);
    }

    public JobSubmissionTask(String name) {
        super(name, Task.JOB_SUBMISSION);
        setRequiredService(1);
    }
}
