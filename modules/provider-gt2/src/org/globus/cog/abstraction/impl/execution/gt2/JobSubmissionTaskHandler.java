// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.gt2;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.globus.gram.GramJobListener;
import org.globus.io.gass.server.GassServer;
import org.globus.io.gass.server.JobOutputListener;
import org.globus.io.gass.server.JobOutputStream;
import org.globus.rsl.NameOpValue;
import org.globus.rsl.RSLParser;
import org.globus.rsl.RslNode;
import org.globus.rsl.Value;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class JobSubmissionTaskHandler implements DelegatedTaskHandler,
        GramJobListener, JobOutputListener {
    static Logger logger = Logger.getLogger(JobSubmissionTaskHandler.class
            .getName());
    private Task task = null;
    private GramJob gramJob;
    private Vector jobList = null;
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
        }
        this.task = task;
        String rsl;
        JobSpecification spec;
        try {
            spec = (JobSpecification) this.task.getSpecification();
        } catch (Exception e) {
            throw new IllegalSpecException(
                    "Exception while retreiving Job Specification", e);
        }
        rsl = prepareSpecification(spec);
        RslNode rslTree = null;
        try {
            rslTree = RSLParser.parse(rsl);
        } catch (Throwable e) {
            throw new IllegalSpecException("Cannot parse the given RSL", e);
        }
        logger.debug("RSL: " + rsl);

        if (rslTree.getOperator() == RslNode.MULTI) {
            this.task.setAttribute("jobCount", "multiple");
            submitMultipleJobs(rslTree, spec);
        } else {
            this.task.setAttribute("jobCount", "single");
            submitSingleJob(rsl, spec);
        }
    }

    private void submitSingleJob(String rsl, JobSpecification spec)
            throws IllegalSpecException, InvalidSecurityContextException,
            InvalidServiceContactException, TaskSubmissionException {
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
        boolean limitedDeleg = (securityContext.getDelegation() != GlobusSecurityContextImpl.FULL_DELEGATION);
        limitedDeleg &= !spec.isDelegationEnabled();
        try {
            // check if the task has not been canceled after it was
            // submitted for execution
            if (this.task.getStatus().getStatusCode() == Status.UNSUBMITTED) {
                this.gramJob.request(server, spec.isBatchJob(), limitedDeleg);
                logger.debug("Submitted job with Globus ID: "
                        + this.gramJob.getIDAsString());
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

    private void submitMultipleJobs(RslNode rslTree, JobSpecification spec)
            throws IllegalSpecException, InvalidSecurityContextException,
            InvalidServiceContactException, TaskSubmissionException {

        MultiJobListener listener = new MultiJobListener(this.task);
        this.jobList = new Vector();
        List jobs = rslTree.getSpecifications();
        Iterator iter = jobs.iterator();
        RslNode node;
        NameOpValue nv;
        String rmc;
        String rsl;
        while (iter.hasNext()) {
            node = (RslNode) iter.next();
            rsl = node.toRSL(true);
            nv = node.getParam("resourceManagerContact");
            if (nv == null) {
                throw new IllegalSpecException(
                        "Error: No resource manager contact for job.");
            } else {
                Object obj = nv.getFirstValue();
                if (obj instanceof Value) {
                    rmc = ((Value) obj).getValue();
                    multiRunSub(rsl, rmc, listener);
                }
                this.task.setStatus(Status.SUBMITTED);
            }
        }
    }

    private void multiRunSub(String rsl, String rmc, MultiJobListener listener)
            throws InvalidSecurityContextException, TaskSubmissionException {
        GramJob job = new GramJob(rsl);

        job.addListener(listener);

        GlobusSecurityContextImpl securityContext = getSecurityContext();
        try {
            job
                    .setCredentials((GSSCredential) securityContext
                            .getCredentials());
        } catch (IllegalArgumentException iae) {
            throw new InvalidSecurityContextException(
                    "Cannot set the SecurityContext twice", iae);
        }

        boolean limitedDeleg = (securityContext.getDelegation() == GlobusSecurityContextImpl.PARTIAL_DELEGATION);
        try {
            job.request(rmc, false, limitedDeleg);
            logger
                    .debug("Submitted job with Globus ID: "
                            + job.getIDAsString());
        } catch (GramException ge) {
            Status newStatus = new StatusImpl();
            Status oldStatus = this.task.getStatus();
            newStatus.setPrevStatusCode(oldStatus.getStatusCode());
            newStatus.setStatusCode(Status.FAILED);
            newStatus.setException(ge);
            this.task.setStatus(newStatus);
            listener.failed(true);
            throw new TaskSubmissionException("Cannot submit job", ge);
        } catch (GSSException gsse) {
            Status newStatus = new StatusImpl();
            Status oldStatus = this.task.getStatus();
            newStatus.setPrevStatusCode(oldStatus.getStatusCode());
            newStatus.setStatusCode(Status.FAILED);
            newStatus.setException(gsse);
            this.task.setStatus(newStatus);
            listener.failed(true);
            throw new InvalidSecurityContextException("Invalid GSSCredentials",
                    gsse);
        }
        listener.runningJob();
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
            String jobCount = (String) this.task.getAttribute("jobCount");
            if (jobCount.equalsIgnoreCase("multiple")) {
                Iterator iterator = this.jobList.iterator();
                while (iterator.hasNext()) {
                    GramJob job = (GramJob) iterator.next();
                    job.cancel();
                }
            } else {
                this.gramJob.cancel();
            }
            this.task.setStatus(Status.CANCELED);
        } catch (GramException ge) {
            cleanup();
            throw new TaskSubmissionException("Cannot cancel job", ge);
        } catch (GSSException gsse) {
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
                String gassURL = startGassServer();
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

            Collection environment = spec.getEnvironment();
            if (environment.size() > 0) {
                String env = "";
                Iterator iterator = environment.iterator();
                while (iterator.hasNext()) {
                    String name = (String) iterator.next();
                    String value = spec.getEnvironmentVariable(name);
                    env += "(" + name + " " + value + ")";
                }
                appendRSL(buf, "environment", env);
            }

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

    private String startGassServer() throws TaskSubmissionException {
        GlobusSecurityContextImpl securityContext = (GlobusSecurityContextImpl) this.task
                .getService(0).getSecurityContext();
        String gassURL = null;

        try {
            this.gassServer = GassServerFactory
                    .getGassServer((GSSCredential) securityContext
                            .getCredentials());
            this.gassServer.registerDefaultDeactivator();
        } catch (Exception e) {
            throw new TaskSubmissionException(
                    "Problems while creating a Gass Server", e);
        }

        gassURL = gassServer.getURL();
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
                Exception e = new GramException(errorCode);
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
        if (gassServer != null) {
            GassServerFactory.decreaseUsageCount(gassServer);
        }
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
                + " job manager is not supported by the GT2 provider");
    }
}
