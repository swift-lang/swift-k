// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.ssh;

import java.net.PasswordAuthentication;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.PublicKeyAuthentication;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
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
            exec = new Exec();
            exec.setCmd(cmd);
            exec.setDir(spec.getDirectory());

            exec.setHost(task.getService(0).getServiceContact().getHost());
            exec.setVerifyhost(false);
            if (task.getService(0).getServiceContact().getPort() != -1) {
                exec.setPort(task.getService(0).getServiceContact().getPort());
            }
            else {
                // default port for ssh
                exec.setPort(22);
                logger.debug("Using default ssh port: 22");
            }

            if (FileLocation.LOCAL.overlaps(spec.getStdOutputLocation()) && notEmpty(spec.getStdOutput())) {
                exec.setOutFile(spec.getStdOutput());
            }
            if (FileLocation.MEMORY.overlaps(spec.getStdOutputLocation())) {
                exec.setOutMem(true);
            }
            
            if (FileLocation.LOCAL.overlaps(spec.getStdErrorLocation()) && notEmpty(spec.getStdError())) {
                exec.setErrFile(spec.getStdError());
            }
            if (FileLocation.MEMORY.overlaps(spec.getStdErrorLocation())) {
                exec.setErrMem(true);
            }
            

            SecurityContext sec = getSecurityContext();

            if (sec.getCredentials() instanceof PasswordAuthentication) {
                PasswordAuthentication auth = (PasswordAuthentication) sec
                        .getCredentials();
                exec.setUsername(auth.getUserName());
                exec.setPassword(new String(auth.getPassword()));
            }
            else if (sec.getCredentials() instanceof PublicKeyAuthentication) {
                PublicKeyAuthentication auth = (PublicKeyAuthentication) sec
                        .getCredentials();
                exec.setUsername(auth.getUsername());
                exec.setPassphrase(new String(auth.getPassPhrase()));
                exec.setKeyfile(auth.getPrivateKeyFile().getAbsolutePath());
            }
            else {
                throw new InvalidSecurityContextException(
                        "Unsupported credentials: " + sec.getCredentials());
            }

            SSHRunner sr = new SSHRunner(exec);
            sr.addListener(this);
            // check if the task has not been canceled after it was submitted
            // for execution
            if (this.task.getStatus().getStatusCode() == Status.UNSUBMITTED) {
                this.task.setStatus(Status.SUBMITTED);
                sr.start();
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
            append(cmd, " ");
            append(cmd, spec.getArgumentsAsString());
        }
        if (FileLocation.LOCAL.overlaps(spec.getStdInputLocation())) {
            throw new IllegalSpecException("The SSH provider does not support local input");
        }
        if (notEmpty(spec.getStdInput())) {
            append(cmd, " <");
            append(cmd, spec.getStdInput());
        }
        if (FileLocation.REMOTE.overlaps(spec.getStdOutputLocation())
                && notEmpty(spec.getStdOutput())) {
            cmd.append(" 1>");
            cmd.append(spec.getStdOutput());
        }
        if (FileLocation.REMOTE.overlaps(spec.getStdErrorLocation())
                && notEmpty(spec.getStdError())) {
            cmd.append(" 2>");
            cmd.append(spec.getStdError());
        }
        cmd.append('\'');
        return cmd.toString();
    }

    private boolean notEmpty(String str) {
        return str != null && !str.equals("");
    }

    private void append(StringBuffer sb, String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\'') {
                sb.append("\\'");
            }
            else {
                sb.append(c);
            }
        }
    }

    private void cleanup() {

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