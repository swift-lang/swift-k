// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.ssh.execution;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.ssh.SSHChannel;
import org.globus.cog.abstraction.impl.ssh.SSHChannelManager;
import org.globus.cog.abstraction.impl.ssh.SSHRunner;
import org.globus.cog.abstraction.impl.ssh.SSHSecurityContextImpl;
import org.globus.cog.abstraction.impl.ssh.SSHTaskStatusListener;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

public class JobSubmissionTaskHandler implements DelegatedTaskHandler,
        SSHTaskStatusListener {
    static Logger logger = Logger.getLogger(JobSubmissionTaskHandler.class
            .getName());
    private Task task = null;
    private Exec exec;
    private SSHChannel s;

    public void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        if (this.task != null) {
            throw new TaskSubmissionException(
                    "JobSubmissionTaskHandler cannot handle two active jobs simultaneously");
        }
        else {
            this.task = task;
            JobSpecification spec;
            try {
                spec = (JobSpecification) this.task.getSpecification();
            }
            catch (Exception e) {
                throw new IllegalSpecException(
                        "Exception while retreiving Job Specification", e);
            }
            // prepare command
            String cmd = prepareSpecification(spec);
            String host = task.getService(0).getServiceContact().getHost();
            int port = task.getService(0).getServiceContact().getPort();

            s = SSHChannelManager.getDefault().getChannel(host,
                    port, getSecurityContext().getCredentials());
            exec = new Exec();
            exec.setCmd(cmd);
            exec.setDir(spec.getDirectory());

            if (FileLocation.LOCAL.overlaps(spec.getStdOutputLocation())
                    && notEmpty(spec.getStdOutput())) {
                exec.setOutFile(spec.getStdOutput());
            }
            if (FileLocation.MEMORY.overlaps(spec.getStdOutputLocation())) {
                exec.setOutMem(true);
            }

            if (FileLocation.LOCAL.overlaps(spec.getStdErrorLocation())
                    && notEmpty(spec.getStdError())) {
                exec.setErrFile(spec.getStdError());
            }
            if (FileLocation.MEMORY.overlaps(spec.getStdErrorLocation())) {
                exec.setErrMem(true);
            }

            SSHRunner r = new SSHRunner(s, exec);
            r.addListener(this);
            // check if the task has not been canceled after it was submitted
            // for execution
            if (this.task.getStatus().getStatusCode() == Status.UNSUBMITTED) {
                this.task.setStatus(Status.SUBMITTED);
                r.startRun(exec);
                this.task.setStatus(Status.ACTIVE);
            }
        }
    }

    public void suspend() throws InvalidSecurityContextException,
            TaskSubmissionException {
        // not implemented yet
    }

    public void resume() throws InvalidSecurityContextException,
            TaskSubmissionException {
        // not implemented yet
    }

    public void cancel() throws InvalidSecurityContextException,
            TaskSubmissionException {
    }

    private String prepareSpecification(JobSpecification spec)
            throws TaskSubmissionException, IllegalSpecException {
        StringBuffer cmd = new StringBuffer("/bin/sh -c '");
        append(cmd, spec.getExecutable());
        if (spec.getArgumentsAsString() != null) {
            cmd.append(' ');
            Iterator i = spec.getArgumentsAsList().iterator();
            while (i.hasNext()) {
                String arg = (String) i.next();
                append(cmd, arg);
                if (i.hasNext()) {
                    cmd.append(' ');
                }
            }
        }
        if (FileLocation.LOCAL.overlaps(spec.getStdInputLocation())) {
            throw new IllegalSpecException(
                    "The SSH provider does not support local input");
        }
        if (notEmpty(spec.getStdInput())) {
            cmd.append(" <");
            append(cmd, spec.getStdInput());
        }
        if (FileLocation.REMOTE.overlaps(spec.getStdOutputLocation())
                && notEmpty(spec.getStdOutput())) {
            cmd.append(" 1>");
            append(cmd, spec.getStdOutput());
        }
        if (FileLocation.REMOTE.overlaps(spec.getStdErrorLocation())
                && notEmpty(spec.getStdError())) {
            cmd.append(" 2>");
            append(cmd, spec.getStdError());
        }
        cmd.append('\'');
        return cmd.toString();
    }

    private boolean notEmpty(String str) {
        return str != null && !str.equals("");
    }
    
    private static boolean[] ESCAPE = new boolean[256];
    
    static {
        ESCAPE['\''] = true;
        ESCAPE[' '] = true;
        ESCAPE['>'] = true;
        ESCAPE['<'] = true;
        ESCAPE['&'] = true;
        ESCAPE['|'] = true;
    }

    private void append(StringBuffer sb, String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c < 256 && c > 0 && ESCAPE[c]) {
                sb.append('\\');
            }
            sb.append(c);
        }
    }
    
    private void space(StringBuffer sb) {
        sb.append(' ');
    }

    private void cleanup() {
        SSHChannelManager.getDefault().releaseChannel(s);
    }

    /*
     * public void outputChanged(String s) { String output =
     * this.task.getStdOutput(); if (output != null) { output += s; } else {
     * output = s; } this.task.setStdOutput(output); }
     */

    public void outputClosed() {
    }

    public void SSHTaskStatusChanged(int status, Exception e) {
        JobSpecification spec = (JobSpecification) this.task.getSpecification();
        if (status == SSHTaskStatusListener.COMPLETED) {
            this.task.setStdOutput(exec.getTaskOutput());
            this.task.setStdError(exec.getTaskError());
            this.task.setStatus(Status.COMPLETED);
        }
        else if (status == SSHTaskStatusListener.FAILED) {
            Status newStatus = new StatusImpl();
            Status oldStatus = this.task.getStatus();
            newStatus.setPrevStatusCode(oldStatus.getStatusCode());
            newStatus.setStatusCode(Status.FAILED);
            newStatus.setException(e);
            this.task.setStatus(newStatus);
        }
        else {
            logger.warn("Unknown status code: " + status);
            return;
        }
        cleanup();
    }

    private SecurityContext getSecurityContext() {
        SecurityContext securityContext = this.task.getService(0)
                .getSecurityContext();
        if (securityContext == null) {
            // create default credentials
            securityContext = new SSHSecurityContextImpl();
        }
        return securityContext;
    }
}