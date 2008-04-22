//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 12, 2008
 */
package org.globus.cog.abstraction.coaster.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.globus.cog.abstraction.impl.common.execution.JobException;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.commands.Command;

public class JobStatusCommand extends Command {
    public static final String NAME = "JOBSTATUS";

    private String taskId;
    private Status status;

    public JobStatusCommand(String taskId, Status status) {
        super(NAME);
        this.taskId = taskId;
        this.status = status;
    }


    public void send() throws ProtocolException {
        try {
            serialize();
        }
        catch (Exception e) {
            throw new ProtocolException(
                    "Could not serialize status", e);
        }
        super.send();
    }

    protected void serialize() throws IOException {
        addOutData(taskId);
        addOutData(String.valueOf(status.getStatusCode()));
        if (status.getException() instanceof JobException) {
            addOutData(String.valueOf(((JobException) status.getException()).getExitCode()));
        }
        else {
            addOutData("0");
        }
        StringBuffer sb = new StringBuffer();
        if (status.getMessage() != null) {
        	sb.append(status.getMessage());
        }
        if (status.getException() != null) {
        	StringWriter sw = new StringWriter();
        	status.getException().printStackTrace(new PrintWriter(sw));
        	sb.append('\n');
        	sb.append(sw.toString());
        }
        addOutData(sb.toString());
    }
}
