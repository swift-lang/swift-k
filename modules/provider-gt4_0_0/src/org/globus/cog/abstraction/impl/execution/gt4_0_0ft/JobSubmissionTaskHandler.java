// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.gt4_0_0ft;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.NonNegativeInteger;
import org.apache.axis.types.PositiveInteger;
import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.execution.gt4_0_0.GlobusSecurityContextImpl;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.exec.client.GramJob;
import org.globus.exec.client.GramJobListener;
import org.globus.exec.generated.JobDescriptionType;
import org.globus.exec.generated.JobTypeEnumeration;
import org.globus.exec.generated.StateEnumeration;
import org.globus.exec.utils.ManagedJobFactoryConstants;
import org.globus.exec.utils.client.ManagedJobFactoryClientHelper;
import org.globus.gram.GramException;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;
import org.globus.wsrf.impl.security.authorization.SelfAuthorization;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class JobSubmissionTaskHandler implements DelegatedTaskHandler,
        GramJobListener {
    static Logger logger = Logger.getLogger(JobSubmissionTaskHandler.class
            .getName());
    private Task task = null;
    private GramJob gramJob;
    private GramJobListener listener = this;

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
                            "Cannot bind the checkpointed task", e1);
                } catch (GSSException e1) {
                    throw new InvalidSecurityContextException(
                            "Exception while retreiving Globus proxy", e1);
                } catch (Exception e1) {
                    throw new TaskSubmissionException("Cannot bind task", e1);
                }
            } else if (st == Status.UNSUBMITTED) {
                doSubmit();
            } else {
                throw new TaskSubmissionException(
                        "Cannot submit COMPLETED, FAILED, or CANCELED tasks");
            }
        }
    }

    private void bind() throws Exception {
        gramJob = new GramJob();
        gramJob.setHandle((String) task.getAttribute("globusID"));
        GlobusSecurityContextImpl securityContext = (GlobusSecurityContextImpl) task
                .getService(0).getSecurityContext();

        try {
            gramJob.setCredentials((GSSCredential) securityContext
                    .getCredentials());
        } catch (IllegalArgumentException iae) {
            throw new InvalidSecurityContextException(
                    "Cannot set the SecurityContext twice", iae);
        }

        Authorization authorization = getAuthorization(securityContext);
        gramJob.setAuthorization(authorization);

        switch (securityContext.getXMLSec()) {
        case GlobusSecurityContextImpl.XML_ENCRYPTION:
            gramJob.setMessageProtectionType(Constants.ENCRYPTION);
            break;
        default:
            gramJob.setMessageProtectionType(Constants.SIGNATURE);
            break;
        }
        gramJob.addListener(listener);

        gramJob.bind();

        logger.debug("Task binding successful");
        logger.debug("Task identity:" + task.getIdentity().toString());
        logger.debug("Checkpoint status = "
                + task.getStatus().getStatusString());
        logger.debug("Refreshing status ...");
        gramJob.refreshStatus();
        // gramJob.bind();

        // The bind does not create a new active thread for the callback.
        // thus we need to do polling to update the task status.
        PollThread pollThread = new PollThread(this.task, this.gramJob);
        Thread thread = new Thread(pollThread);
        logger.debug("Started the polling thread");
        thread.start();

    }

    private void doSubmit() throws TaskSubmissionException,
            IllegalSpecException, InvalidSecurityContextException,
            InvalidServiceContactException {
        JobDescriptionType rsl;
        JobSpecification spec;
        try {
            spec = (JobSpecification) this.task.getSpecification();
        } catch (Exception e) {
            throw new IllegalSpecException(
                    "Exception while retreiving Job Specification", e);
        }

        ServiceContact serviceContact = this.task.getService(0)
                .getServiceContact();
        String server = serviceContact.getContact();
        EndpointReferenceType factoryEndpoint;
        try {
            URL factoryURL = ManagedJobFactoryClientHelper
                    .getServiceURL(server).getURL();
            factoryEndpoint = ManagedJobFactoryClientHelper
                    .getFactoryEndpoint(factoryURL,
                            ManagedJobFactoryConstants.DEFAULT_FACTORY_TYPE);
        } catch (Exception e) {
            throw new IllegalSpecException("Invalid service factory", e);
        }

        rsl = prepareSpecification(spec);

        this.gramJob = new GramJob(rsl);

        GlobusSecurityContextImpl securityContext = (GlobusSecurityContextImpl) this.task
                .getService(0).getSecurityContext();

        try {
            this.gramJob.setCredentials((GSSCredential) securityContext
                    .getCredentials());
        } catch (IllegalArgumentException iae) {
            throw new InvalidSecurityContextException(
                    "Cannot set the SecurityContext twice", iae);
        }

        Authorization authorization = getAuthorization(securityContext);
        this.gramJob.setAuthorization(authorization);

        switch (securityContext.getXMLSec()) {
        case GlobusSecurityContextImpl.XML_ENCRYPTION:
            this.gramJob.setMessageProtectionType(Constants.ENCRYPTION);
            break;
        default:
            this.gramJob.setMessageProtectionType(Constants.SIGNATURE);
            break;
        }

        if (!spec.isBatchJob()) {
            this.gramJob.addListener(this);
        }

        try {
            this.gramJob.submit(factoryEndpoint, spec.isBatchJob(), true,
                    "uuid:" + UUIDGenFactory.getUUIDGen().nextUUID());
            this.task.setAttribute("globusID", this.gramJob.getHandle());
            this.task.setStatus(Status.SUBMITTED);
            logger.info("Job submitted");
            if (spec.isBatchJob()) {
                this.task.setStatus(Status.COMPLETED);
            }
        } catch (Exception e) {
            Status newStatus = new StatusImpl();
            Status oldStatus = this.task.getStatus();
            newStatus.setPrevStatusCode(oldStatus.getStatusCode());
            newStatus.setStatusCode(Status.FAILED);
            newStatus.setException(e);
            this.task.setStatus(newStatus);
            cleanup();
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
            this.gramJob.cancel();
            this.task.setStatus(Status.CANCELED);
            cleanup();
        } catch (Exception e) {
            cleanup();
            throw new TaskSubmissionException("Cannot cancel job", e);
        }
    }

    private JobDescriptionType prepareSpecification(JobSpecification spec)
            throws IllegalSpecException, TaskSubmissionException {
        // if the job specification is explicitly specified
        JobDescriptionType desc = new JobDescriptionType();

        if (spec.getDirectory() != null) {
            desc.setDirectory(spec.getDirectory());
        }
        if (spec.getAttribute("count") != null) {
            desc.setCount(new PositiveInteger(spec.getAttribute("count")
                    .toString()));
        }
        if (spec.getStdInput() != null) {
            desc.setStdin(spec.getStdInput());
        }
        if (spec.getAttribute("maxCpuTime") != null) {
            desc.setMaxCpuTime(new Long(spec.getAttribute("maxCpuTime")
                    .toString()));
        }
        if (spec.getAttribute("hostCount") != null) {
            desc.setHostCount(new PositiveInteger(spec
                    .getAttribute("hostCount").toString()));
        }
        if (spec.getAttribute("jobType") != null) {
            desc.setJobType(JobTypeEnumeration.fromString(spec.getAttribute(
                    "jobType").toString()));
        }
        if (spec.getAttribute("maxMemory") != null) {
            desc.setMaxMemory(new NonNegativeInteger(spec.getAttribute(
                    "maxMemory").toString()));
        }
        if (spec.getAttribute("maxTime") != null) {
            desc.setMaxTime(new Long(spec.getAttribute("maxTime").toString()));
        }
        if (spec.getAttribute("maxWallTime") != null) {
            desc.setMaxWallTime(new Long(spec.getAttribute("maxWallTime")
                    .toString()));
        }
        if (spec.getAttribute("minMemory") != null) {
            desc.setMinMemory(new NonNegativeInteger(spec.getAttribute(
                    "minMemory").toString()));
        }
        if (spec.getAttribute("project") != null) {
            desc.setProject((String) spec.getAttribute("project"));
        }
        if (spec.getAttribute("queue") != null) {
            desc.setQueue((String) spec.getAttribute("queue"));
        }

        Vector v = spec.getArgumentsAsVector();
        desc.setArgument((String[]) v.toArray(new String[0]));

        boolean batchJob = spec.isBatchJob();
        if (spec.isRedirected()) {
            throw new IllegalSpecException(
                    "The gt4.0.0ft provider does not support redirection");
        }
        boolean localExecutable = spec.isLocalExecutable();

        String gassURL = null;
        if (localExecutable) {
            throw new IllegalSpecException(
                    "The gt4.0.0ft provider does not support file staging");
        }

        desc.setExecutable(spec.getExecutable());
        desc.setStdout(spec.getStdOutput());
        desc.setStderr(spec.getStdError());

        return desc;
    }

    public void stateChanged(GramJob job) {
        StateEnumeration state = job.getState();
        if (state.equals(StateEnumeration.Active)) {
            this.task.setStatus(Status.ACTIVE);
        } else if (state.equals(StateEnumeration.Failed)) {
            Status newStatus = new StatusImpl();
            Status oldStatus = this.task.getStatus();
            newStatus.setPrevStatusCode(oldStatus.getStatusCode());
            newStatus.setStatusCode(Status.FAILED);
            int errorCode = job.getError();
            newStatus.setMessage("#" + errorCode + " "
                    + job.getFault().getDescription()[0]);
            newStatus.setException((Exception) job.getFault().getCause());
            this.task.setStatus(newStatus);
            cleanup();
        } else if (state.equals(StateEnumeration.Done)) {
            this.task.setStatus(Status.COMPLETED);
            cleanup();
        } else if (state.equals(StateEnumeration.Suspended)) {
            this.task.setStatus(Status.SUSPENDED);
        } else if (state.equals(StateEnumeration.Pending)) {
            this.task.setStatus(Status.SUBMITTED);
        } else {
            logger.debug("Unknown status: " + state.getValue());
        }
    }

    private void cleanup() {
        this.gramJob.removeListener(this);
        logger.debug("Destroying remote service for task "
                + this.task.getIdentity().toString());
        try {
            this.gramJob.destroy();
        } catch (Exception e) {
            logger.warn("Unable to destroy remote service for task "
                    + this.task.getIdentity().toString(), e);
        }
    }

    private Authorization getAuthorization(
            GlobusSecurityContextImpl securityContext) {
        Authorization authorization = HostAuthorization.getInstance();

        org.globus.gsi.gssapi.auth.Authorization auth = securityContext
                .getAuthorization();
        // map to GT4 authorization
        if (auth instanceof org.globus.gsi.gssapi.auth.SelfAuthorization) {
            authorization = SelfAuthorization.getInstance();
        } else if (auth instanceof org.globus.gsi.gssapi.auth.HostAuthorization) {
            authorization = HostAuthorization.getInstance();
        }
        return authorization;
    }
}