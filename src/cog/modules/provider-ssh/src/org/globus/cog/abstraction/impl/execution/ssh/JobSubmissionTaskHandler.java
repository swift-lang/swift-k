// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.ssh;

import java.io.FileWriter;
import java.io.IOException;
import java.net.PasswordAuthentication;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.PublicKeyAuthentication;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;
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
        } else {
            this.task = task;
            JobSpecification spec;
            try {
                spec = (JobSpecification) this.task.getSpecification();
            } catch (Exception e) {
                throw new IllegalSpecException(
                        "Exception while retreiving Job Specification", e);
            }
            //prepare command
            String cmd = prepareSpecification(spec);
            exec = new Exec();
            exec.setCmd(cmd);
            exec.setDir(spec.getDirectory());

            exec.setHost(task.getService(0).getServiceContact().getHost());
            exec.setVerifyhost(false);
            if (task.getService(0).getServiceContact().getPort() != -1) {
                exec.setPort(task.getService(0).getServiceContact().getPort());
            } else {
                // default port for ssh
                exec.setPort(22);
                logger.debug("Using default ssh port: 22");
            }

            if (spec.getStdOutput() != null) {
                exec.setOutput(false);
            }
            if (spec.getStdError() != null) {
                exec.setError(false);
            }

            if (spec.isRedirected()) {
                exec.setError(true);
                exec.setOutput(true);
            }

            SecurityContext sec = getSecurityContext();

            if (sec.getCredentials() instanceof PasswordAuthentication) {
                PasswordAuthentication auth = (PasswordAuthentication) sec
                        .getCredentials();
                exec.setUsername(auth.getUserName());
                exec.setPassword(new String(auth.getPassword()));
            } else if (sec.getCredentials() instanceof PublicKeyAuthentication) {
                PublicKeyAuthentication auth = (PublicKeyAuthentication) sec
                        .getCredentials();
                exec.setUsername(auth.getUsername());
                exec.setPassphrase(new String(auth.getPassPhrase()));
                exec.setKeyfile(auth.getPrivateKeyFile().getAbsolutePath());
            } else {
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
            throws TaskSubmissionException {
        //this will only work with sh/bash
        StringBuffer cmd = new StringBuffer(spec.getExecutable());
        if (spec.getArgumentsAsString() != null) {
            cmd.append(" ");
            cmd.append(spec.getArgumentsAsString());
        }
        if ((spec.getStdInput() != null) && (!spec.getStdInput().equals(""))) {
            cmd.append(" <");
            cmd.append(spec.getStdInput());
        }
        if (!spec.isRedirected()) {
            if ((spec.getStdOutput() != null)
                    && (!spec.getStdOutput().equals(""))) {
                cmd.append(" 1>");
                cmd.append(spec.getStdOutput());
            }
            if ((spec.getStdError() != null)
                    && (!spec.getStdError().equals(""))) {
                cmd.append(" 2>");
                cmd.append(spec.getStdError());
            }
        }
        return cmd.toString();
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

    public void SSHTaskStatusChanged(int status, String message) {
        JobSpecification spec = (JobSpecification) this.task.getSpecification();
        if (status == SSHTaskStatusListener.COMPLETED) {
            if ((spec.isRedirected()) && (spec.getStdOutput() != null)
                    && (!spec.getStdOutput().equals(""))) {
                try {
                    FileWriter writer = new FileWriter(spec.getStdOutput());
                    writer.write(exec.getTaskOutput());
                    writer.flush();
                    writer.close();
                } catch (IOException ioe) {
                    Status newStatus = new StatusImpl();
                    Status oldStatus = this.task.getStatus();
                    newStatus.setPrevStatusCode(oldStatus.getStatusCode());
                    newStatus.setStatusCode(Status.FAILED);
                    newStatus.setException(ioe);
                    newStatus.setMessage(ioe.getMessage());
                    this.task.setStatus(newStatus);
                    return;
                }
            } else {
                this.task.setStdOutput(exec.getTaskOutput());
            }
            this.task.setStatus(Status.COMPLETED);
        } else if (status == SSHTaskStatusListener.FAILED) {
            Status newStatus = new StatusImpl();
            Status oldStatus = this.task.getStatus();
            newStatus.setPrevStatusCode(oldStatus.getStatusCode());
            newStatus.setStatusCode(Status.FAILED);
            Exception e = new Exception(message);
            newStatus.setException(e);
            this.task.setStatus(newStatus);
        } else {
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