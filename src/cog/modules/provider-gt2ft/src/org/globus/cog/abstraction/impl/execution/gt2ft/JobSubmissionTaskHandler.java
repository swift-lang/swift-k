// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.gt2ft;

import java.net.MalformedURLException;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.execution.gt2.GlobusSecurityContextImpl;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.gram.Gram;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.globus.gram.GramJobListener;
import org.globus.io.gass.server.GassServer;
import org.globus.io.gass.server.JobOutputListener;
import org.globus.io.gass.server.JobOutputStream;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class JobSubmissionTaskHandler implements DelegatedTaskHandler,
        GramJobListener, JobOutputListener {
    static Logger logger = Logger.getLogger(JobSubmissionTaskHandler.class
            .getName());

    private Task task = null;
    private GramJob gramJob;
    private boolean startGassServer = false;
    private GassServer gassServer = null;
    private JobOutputStream stdoutStream;
    private JobOutputStream stderrStream;

    public void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        if (this.task != null) {
            throw new TaskSubmissionException(
                    "JobSubmissionTaskHandler cannot handle two active jobs simultaneously");
        } else {
            this.task = task;
            int st = this.task.getStatus().getStatusCode();
            if (st == Status.ACTIVE || st == Status.SUBMITTED
                    || st == Status.SUSPENDED) {
                try {
                    bind();
                } catch (MalformedURLException e1) {
                    throw new TaskSubmissionException(
                            "Cannot extract globusID from available task attributes",
                            e1);
                } catch (GramException e1) {
                    throw new TaskSubmissionException(
                            "Cannot bind the checkpointed task. There can be two reasons for this\n"
                                    + "(1) The globusid in the checkpoint file is incorrect\n"
                                    + "(2) The task has completed on the remote machine",
                            e1);
                } catch (GSSException e1) {
                    throw new InvalidSecurityContextException(
                            "Exception while retreiving Globus proxy", e1);
                }
            } else if (st == Status.UNSUBMITTED) {
                doSubmit();
            } else {
                throw new TaskSubmissionException(
                        "Cannot submit COMPLETED, FAILED, or CANCELED tasks");
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
        try {
            if (this.task.getStatus().getStatusCode() == Status.UNSUBMITTED) {
                this.task.setStatus(Status.CANCELED);
                return;
            }

            this.gramJob.cancel();
            this.task.setStatus(Status.CANCELED);
            cleanup();
        } catch (GramException ge) {
            cleanup();
            throw new TaskSubmissionException("Cannot cancel job", ge);
        } catch (GSSException gsse) {
            cleanup();
            throw new InvalidSecurityContextException("Invalid GSSCredentials",
                    gsse);
        }
    }

    private void bind() throws MalformedURLException, GramException,
            GSSException, TaskSubmissionException {
        this.gramJob = new GramJob(null);
        this.gramJob.setID((String) task.getAttribute("globusID"));
        this.gramJob.setCredentials((GSSCredential) getSecurityContext()
                .getCredentials());
        this.gramJob.bind();
        this.gramJob.addListener(this);
        logger.debug("Task binding successful");
        logger.debug("Task identity:" + this.task.getIdentity().toString());
        logger.debug("Checkpoint status = "
                + task.getStatus().getStatusString());
        Gram.jobStatus(this.gramJob);
        if (((JobSpecification) this.task.getSpecification()).isRedirected()) {
            String gassUrl = startGassServer(Integer
                    .parseInt((String) this.task.getAttribute("gassPort")));
            logger.debug("Gass url: " + gassUrl);
        }
    }

    private void doSubmit() throws TaskSubmissionException,
            IllegalSpecException, InvalidSecurityContextException,
            InvalidServiceContactException {
        String rsl;
        JobSpecification spec;
        try {
            spec = (JobSpecification) this.task.getSpecification();
        } catch (Exception e) {
            throw new IllegalSpecException(
                    "Exception while retreiving Job Specification", e);
        }
        rsl = prepareSpecification(spec);
        logger.debug("RSL: " + rsl);
        this.gramJob = new GramJob(rsl);
        GlobusSecurityContextImpl securityContext = getSecurityContext();
        try {
            this.gramJob.setCredentials((GSSCredential) securityContext
                    .getCredentials());
        } catch (IllegalArgumentException iae) {
            throw new InvalidSecurityContextException(
                    "Cannot set the SecurityContext twice", iae);
        }

        if (!spec.isBatchJob()) {
            this.gramJob.addListener(this);
        }

        ServiceContact serviceContact = this.task.getService(0)
                .getServiceContact();
        String server = serviceContact.getContact();

        // if the jobmanager attribute is specified, handle it
        String jobmanager = (String) this.task.getService(0).getAttribute(
                "jobmanager");
        if (jobmanager != null) {
            server = handleJobManager(server, jobmanager);
        }
        logger.debug("Execution server: " + server);
        boolean limitedDeleg = (securityContext.getDelegation() == GlobusSecurityContextImpl.PARTIAL_DELEGATION);
        try {
            // check if the task has not been canceled after it was
            // submitted for execution
            if (this.task.getStatus().getStatusCode() == Status.UNSUBMITTED) {
                this.gramJob.request(server, spec.isBatchJob(), limitedDeleg);
                this.task
                        .setAttribute("globusID", this.gramJob.getIDAsString());
                this.task.setStatus(Status.SUBMITTED);
                if (spec.isBatchJob()) {
                    this.task.setStatus(Status.COMPLETED);
                }
            }
        } catch (GramException ge) {
            Status newStatus = new StatusImpl();
            Status oldStatus = this.task.getStatus();
            newStatus.setPrevStatusCode(oldStatus.getStatusCode());
            newStatus.setStatusCode(Status.FAILED);
            newStatus.setException(ge);
            this.task.setStatus(newStatus);
            cleanup();
            throw new TaskSubmissionException("Cannot submit job", ge);
        } catch (GSSException gsse) {
            Status newStatus = new StatusImpl();
            Status oldStatus = this.task.getStatus();
            newStatus.setPrevStatusCode(oldStatus.getStatusCode());
            newStatus.setStatusCode(Status.FAILED);
            newStatus.setException(gsse);
            this.task.setStatus(newStatus);
            cleanup();
            throw new InvalidSecurityContextException("Invalid GSSCredentials",
                    gsse);
        }
    }

    private String prepareSpecification(JobSpecification spec)
            throws IllegalSpecException, TaskSubmissionException {
        if (spec.getSpecification() != null) {
            return spec.getSpecification();
        } else {
            StringBuffer buf = new StringBuffer("&");
            boolean batchJob = spec.isBatchJob();
            boolean redirected = spec.isRedirected();
            boolean localExecutable = spec.isLocalExecutable();
            boolean localInput = spec.isLocalInput();

            if (batchJob && redirected) {
                throw new IllegalSpecException(
                        "Cannot redirect the output/error of a batch job");
            }

            if (redirected || localExecutable || localInput) {
                this.startGassServer = true;
                String gassURL = startGassServer(0);
                appendRSL(buf, "rsl_substitution", "(GLOBUSRUN_GASS_URL "
                        + gassURL + ")");
            }
            // sets the executable
            if (this.startGassServer && localExecutable) {
                appendRSL(buf, "executable", "$(GLOBUSRUN_GASS_URL)"
                        + spec.getExecutable());
            } else {
                appendRSL(buf, "executable", spec.getExecutable());
            }
            // sets other parameters
            appendRSL(buf, "arguments", spec.getArgumentsAsString());
            appendRSL(buf, "directory", spec.getDirectory());

            // sets the stdin
            if (this.startGassServer && localInput) {
                appendRSL(buf, "stdin", "$(GLOBUSRUN_GASS_URL)"
                        + spec.getStdInput());
            } else {
                appendRSL(buf, "stdin", spec.getStdInput());
            }

            // if output is to be redirected
            if (this.startGassServer && redirected) {
                // if no output file is specified, use the stdout
                if ((spec.getStdOutput() == null)
                        || (spec.getStdOutput().equals(""))) {
                    appendRSL(buf, "stdout",
                            "$(GLOBUSRUN_GASS_URL)/dev/stdout-"
                                    + this.task.getIdentity().toString());
                } else {
                    appendRSL(buf, "stdout", "$(GLOBUSRUN_GASS_URL)/"
                            + spec.getStdOutput());
                }
            } else {
                // output on the remote machine
                appendRSL(buf, "stdout", spec.getStdOutput());
            }
            // if error is to be redirected
            if (this.startGassServer && redirected) {
                // if no error file is specified, use the stdout
                if ((spec.getStdError() == null)
                        || (spec.getStdError().equals(""))) {
                    appendRSL(buf, "stderr",
                            "$(GLOBUSRUN_GASS_URL)/dev/stderr-"
                                    + this.task.getIdentity().toString());
                } else {
                    appendRSL(buf, "stderr", "$(GLOBUSRUN_GASS_URL)/"
                            + spec.getStdError());
                }
            } else {
                // error on the remote machine
                appendRSL(buf, "stderr", spec.getStdError());
            }

            Enumeration en = spec.getAllAttributes();
            while (en.hasMoreElements()) {
                try {
                    String key = (String) en.nextElement();
                    appendRSL(buf, key, (String) spec.getAttribute(key));
                } catch (Exception e) {
                    throw new IllegalSpecException(
                            "Cannot parse the user defined attributes");
                }
            }
            return buf.toString();
        }
    }

    private void appendRSL(StringBuffer rsl, String attribute, String value) {
        if (value == null || value.length() == 0) {
            return;
        }
        rsl.append("(");
        rsl.append(attribute);
        rsl.append("=");
        rsl.append(value);
        rsl.append(")");
    }

    private String startGassServer(int port) throws TaskSubmissionException {
        GlobusSecurityContextImpl securityContext = getSecurityContext();
        String gassURL = null;
        if (this.gassServer == null) {
            try {
                this.gassServer = GassServerFactory.getGassServer(
                        (GSSCredential) securityContext.getCredentials(), port);
            } catch (Exception e) {
                throw new TaskSubmissionException(
                        "Problems while creating a Gass Server", e);
            }
        }
        gassServer.registerDefaultDeactivator();
        gassURL = gassServer.getURL();
        this.task
                .setAttribute("gassPort", String.valueOf(gassServer.getPort()));
        //    this.task.setAttribute("gassServer", this.gassServer);
        this.stdoutStream = new JobOutputStream(this);
        this.stderrStream = new JobOutputStream(this);

        gassServer.registerJobOutputStream("err-"
                + this.task.getIdentity().toString(), this.stderrStream);
        gassServer.registerJobOutputStream("out-"
                + this.task.getIdentity().toString(), this.stdoutStream);
        logger.debug("Started the GASS server");
        return gassURL;
    }

    public void statusChanged(GramJob job) {
        int status = job.getStatus();
        switch (status) {
        case 2:
            this.task.setStatus(Status.ACTIVE);
            break;
        case 4:
            Status newStatus = new StatusImpl();
            Status oldStatus = this.task.getStatus();
            newStatus.setPrevStatusCode(oldStatus.getStatusCode());
            newStatus.setStatusCode(Status.FAILED);
            int errorCode = job.getError();
            Exception e = new Exception("Error code: " + errorCode);
            newStatus.setException(e);
            this.task.setStatus(newStatus);
            break;
        case 8:
            this.task.setStatus(Status.COMPLETED);
            break;
        case 16:
            this.task.setStatus(Status.SUSPENDED);
            break;
        case 32:
            this.task.setStatus(Status.UNSUBMITTED);
            break;
        default:
            break;
        }
        if ((status == 4) || (status == 8)) {
            cleanup();
        }
    }

    private void cleanup() {
        this.gramJob.removeListener(this);
        // do not shutdown the gass server since it is being shared
        /*
         * try { if (this.startGassServer) { this.gassServer.shutdown(); } }
         * catch (Exception e) { e.printStackTrace(); }
         */
    }

    public void outputChanged(String s) {
        String output = this.task.getStdOutput();
        if (output == null) {
            output = s;
        } else {
            output += s;
        }
        this.task.setStdOutput(output);
    }

    public void outputClosed() {
    }

    private GlobusSecurityContextImpl getSecurityContext() {
        GlobusSecurityContextImpl securityContext = (GlobusSecurityContextImpl) this.task
                .getService(0).getSecurityContext();
        if (securityContext == null) {
            // create default credentials
            securityContext = new GlobusSecurityContextImpl();
        }
        return securityContext;
    }

    private String handleJobManager(String server, String jobmanager)
            throws InvalidServiceContactException {
        if (jobmanager.equalsIgnoreCase(ExecutionService.FORK_JOBMANAGER)
                || jobmanager.equalsIgnoreCase("jobmanager-"
                        + ExecutionService.FORK_JOBMANAGER)) {
            logger.debug("Using the FORK jobmanager: " + server
                    + "/jobmanager-fork");
            return server + "/jobmanager-fork";
        } else if (jobmanager.equalsIgnoreCase(ExecutionService.PBS_JOBMANAGER)
                || jobmanager.equalsIgnoreCase("jobmanager-"
                        + ExecutionService.PBS_JOBMANAGER)) {
            logger.debug("Using the PBS jobmanager: " + server
                    + "/jobmanager-pbs");
            return server + "/jobmanager-pbs";
        }
        throw new InvalidServiceContactException(jobmanager
                + " job manager is not supported by the GT2FT provider");
    }
}