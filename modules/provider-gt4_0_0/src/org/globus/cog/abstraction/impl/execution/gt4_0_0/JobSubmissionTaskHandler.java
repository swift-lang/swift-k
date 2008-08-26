// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.gt4_0_0;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.NonNegativeInteger;
import org.apache.axis.types.PositiveInteger;
import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionProperties;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;
import org.globus.cog.abstraction.interfaces.Delegation;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.exec.client.GramJob;
import org.globus.exec.client.GramJobListener;
import org.globus.exec.generated.ExtensionsType;
import org.globus.exec.generated.FaultType;
import org.globus.exec.generated.JobDescriptionType;
import org.globus.exec.generated.JobTypeEnumeration;
import org.globus.exec.generated.NameValuePairType;
import org.globus.exec.generated.StateEnumeration;
import org.globus.exec.utils.ManagedJobFactoryConstants;
import org.globus.exec.utils.client.ManagedJobFactoryClientHelper;
import org.globus.exec.utils.rsl.RSLParseException;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;
import org.globus.wsrf.impl.security.authorization.SelfAuthorization;
import org.ietf.jgss.GSSCredential;

/**
 * @author CoG Team
 * @author David Del Vecchio
 */
public class JobSubmissionTaskHandler implements DelegatedTaskHandler,
        GramJobListener {
    static Logger logger = Logger.getLogger(JobSubmissionTaskHandler.class);

    private Task task = null;
    private GramJob gramJob;
    private boolean canceling;

    private static Map jmMappings;

    static {
        jmMappings = new HashMap();
        jmMappings.put("fork", "Fork");
        jmMappings.put("pbs", "PBS");
        jmMappings.put("lsf", "LSF");
        jmMappings.put("condor", "Condor");
        jmMappings.put("sge", "SGE");
    }

    public void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        if (this.task != null) {
            throw new TaskSubmissionException(
                    "JobSubmissionTaskHandler cannot handle two active jobs simultaneously");
        }
        else {
            this.task = task;
            task.setStatus(Status.SUBMITTING);

            JobDescriptionType rsl;
            JobSpecification spec;
            try {
                spec = (JobSpecification) this.task.getSpecification();
            }
            catch (Exception e) {
                throw new IllegalSpecException(
                        "Exception while retreiving Job Specification", e);
            }

            Service service = this.task.getService(0);
            ServiceContact serviceContact = service.getServiceContact();
            String server = serviceContact.getContact();

            String factoryType = ManagedJobFactoryConstants.DEFAULT_FACTORY_TYPE;
            if (service instanceof ExecutionService) {
                String jobManager = ((ExecutionService) service)
                        .getJobManager();
                if (logger.isDebugEnabled()) {
                    logger.debug("Requested job manager: " + jobManager);
                }
                if (jobManager != null) {
                    String lc = jobManager.toLowerCase();
                    if (lc.startsWith("jobmanager-")) {
                        lc = lc.substring(11);
                    }
                    if (jmMappings.containsKey(lc)) {
                        jobManager = (String) jmMappings.get(lc);
                    }
                    factoryType = jobManager;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Actual job manager: " + factoryType);
                }
            }

            EndpointReferenceType factoryEndpoint;
            URL factoryURL;

            try {
                factoryURL = ManagedJobFactoryClientHelper
                        .getServiceURL(server).getURL();

                // Fix (hack) for the above returning the wrong default port
                if (server.indexOf(':') == -1) {
                    factoryURL = new URL(factoryURL.getProtocol(), factoryURL
                            .getHost(), 8443, factoryURL.getFile());
                }

                factoryEndpoint = ManagedJobFactoryClientHelper
                        .getFactoryEndpoint(factoryURL, factoryType);
            }
            catch (Exception e) {
                throw new IllegalSpecException("Invalid service factory", e);
            }

            if (spec.getSpecification() != null) {
                try {
                    this.gramJob = new GramJob(spec.getSpecification());
                }
                catch (RSLParseException e) {
                    throw new IllegalSpecException(e.getMessage(), e);
                }
            }
            else {
                rsl = prepareSpecification(spec, server);
                this.gramJob = new GramJob(rsl);
            }

            GlobusSecurityContextImpl securityContext = (GlobusSecurityContextImpl) this.task
                    .getService(0).getSecurityContext();

            try {
                this.gramJob.setCredentials((GSSCredential) securityContext
                        .getCredentials());
            }
            catch (IllegalArgumentException iae) {
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

            setMiscJobParams(spec, this.gramJob);

            try {
                if (logger.isInfoEnabled()) {
                    logger.info("Submitting task: " + task);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Rsl is " + this.gramJob.toString());
                    }
                }
                this.gramJob.submit(factoryEndpoint, spec.isBatchJob(), spec
                        .getDelegation() != Delegation.NO_DELEGATION,
                        "uuid:" + UUIDGenFactory.getUUIDGen().nextUUID());
                if (logger.isInfoEnabled()) {
                    logger.info("Task submitted: " + task);
                }
                if (spec.isBatchJob()) {
                    this.task.setStatus(Status.COMPLETED);
                }
            }
            catch (Exception e) {
                // No need for cleanup. Reportedly no resource has been created
                // if an exception is thrown
                gramJob.removeListener(this);
                throw new TaskSubmissionException("Cannot submit job: "
                        + e.getMessage(), e);
            }
        }
    }

    protected void setMiscJobParams(JobSpecification spec, GramJob job) {
        job
                .setDelegationEnabled(spec.getDelegation() != Delegation.NO_DELEGATION);

        Object soTimeout = spec.getAttribute("socketTimeout");
        if (soTimeout instanceof Integer) {
            job.setTimeOut(((Integer) soTimeout).intValue());
        }
        else if (soTimeout instanceof String) {
            job.setTimeOut(Integer.parseInt((String) soTimeout));
        }
        else if (soTimeout != null) {
            logger.warn("Unknown value for socketTimeout attribute ("
                    + soTimeout + "). Ignoring.");
        }
    }

    public void suspend() throws InvalidSecurityContextException,
            TaskSubmissionException {
        throw new UnsupportedOperationException("suspend");
    }

    public void resume() throws InvalidSecurityContextException,
            TaskSubmissionException {
        throw new UnsupportedOperationException("resume");
    }

    public void cancel() throws InvalidSecurityContextException,
            TaskSubmissionException {
        try {
            synchronized (task) {
                canceling = true;
            }
            this.gramJob.cancel();
            // no cleanup since cancel() calls destroy()
        }
        catch (Exception e) {
            throw new TaskSubmissionException("Cannot cancel job", e);
        }
    }

    private static final FileLocation REDIRECT_LOCATION = FileLocation.MEMORY
            .and(FileLocation.LOCAL);

    private JobDescriptionType prepareSpecification(JobSpecification spec,
            String server) throws IllegalSpecException, TaskSubmissionException {
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
            desc.setMaxWallTime(wallTimeToMinutes(spec.getAttribute("maxWallTime")));
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

        if (spec.getAttribute("host_types") != null) {
            MessageElement nodesMessageElement = new MessageElement("",
                    "nodes", spec.getAttribute("host_types"));
            MessageElement[] messageElements = new MessageElement[] { nodesMessageElement };
            ExtensionsType extensions = new ExtensionsType();
            extensions.set_any(messageElements);
            desc.setExtensions(extensions);
        }

        desc.setArgument((String[]) spec.getArgumentsAsList().toArray(
                new String[0]));

        boolean batchJob = spec.isBatchJob();
        if (FileLocation.MEMORY_AND_LOCAL.overlaps(spec.getStdOutputLocation()
                .and(spec.getStdErrorLocation()))) {
            complainAboutRedirection();
        }
        else {
            if (spec.getStdInput() != null) {
                desc.setStdin(spec.getStdInput());
            }
            if (spec.getStdOutput() != null) {
                desc.setStdout(spec.getStdOutput());
            }
            if (spec.getStdError() != null) {
                desc.setStderr(spec.getStdError());
            }
        }

        if (FileLocation.LOCAL.overlaps(spec.getExecutableLocation())) {
            throw new IllegalSpecException(
                    "The GT4.0.x provider does not support local executables");
        }
        desc.setExecutable(spec.getExecutable());

        Collection environment = spec.getEnvironmentVariableNames();
        if (environment != null && environment.size() > 0) {
            NameValuePairType[] envVars = new NameValuePairType[environment
                    .size()];
            Iterator iterator = environment.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                String name = (String) iterator.next();
                String value = spec.getEnvironmentVariable(name);
                envVars[i++] = new NameValuePairType(name, value);
            }
            desc.setEnvironment(envVars);
        }

        return desc;
    }

    private static boolean redirectionWarning;

    private static void complainAboutRedirection() throws IllegalSpecException,
            TaskSubmissionException {
        try {
            if ("true".equals(AbstractionProperties.getProperties("gt4")
                    .getProperty("fail.on.redirect"))) {
                throw new IllegalSpecException(
                        "The GT4.0.x provider does not support redirection");
            }
            else {
                synchronized (JobSubmissionTaskHandler.class) {
                    if (!redirectionWarning) {
                        redirectionWarning = true;
                        logger
                                .warn("The GT4 provider does not support redirection. "
                                        + "Redirection requests will be ignored without further warnings.");
                    }
                }
            }
        }
        catch (InvalidProviderException e) {
            throw new TaskSubmissionException("Cannot get provider properties",
                    e);
        }
    }

    public void stateChanged(GramJob job) {
        boolean cleanup = false;
        StateEnumeration state = job.getState();
        if (state.equals(StateEnumeration.Active)) {
            this.task.setStatus(Status.ACTIVE);
        }
        else if (state.equals(StateEnumeration.Failed)) {
            boolean canceled = false;
            synchronized (task) {
                if (canceling) {
                    canceled = true;
                }
            }
            if (canceled) {
                this.task.setStatus(Status.CANCELED);
                this.gramJob.removeListener(this);
            }
            else {
                if (job.getFault() != null) {
                    failTask(job.getFault().getDescription()[0] + ", "
                            + getCauses(job.getFault()), null);
                }
                else {
                    int errorCode = job.getError();
                    failTask("#" + errorCode, null);
                }
                cleanup = true;
            }
        }
        else if (state.equals(StateEnumeration.Done)) {
            if (job.getExitCode() != 0) {
                failTask(
                        "Job failed with an exit code of " + job.getExitCode(),
                        null);
            }
            else {
                this.task.setStatus(Status.COMPLETED);
            }
            cleanup = true;
        }
        else if (state.equals(StateEnumeration.Suspended)) {
            this.task.setStatus(Status.SUSPENDED);
        }
        else if (state.equals(StateEnumeration.Pending)) {
            this.task.setStatus(Status.SUBMITTED);
        }
        else {
            logger.debug("Unknown status: " + state.getValue());
        }
        if (cleanup) {
            try {
                cleanup();
            }
            catch (Exception e) {
                logger.warn("Unable to destroy remote service for task "
                        + this.task.getIdentity().toString(), e);
            }
        }
    }

    private String getCauses(FaultType f) {
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < f.getFaultCause().length; i++) {
            sb.append(f.getFaultCause(i).getDescription()[0]);
            sb.append("\n");
        }
        return sb.toString();
    }

    private void failTask(String message, Exception exception) {
        Status newStatus = new StatusImpl();
        Status oldStatus = this.task.getStatus();
        newStatus.setPrevStatusCode(oldStatus.getStatusCode());
        newStatus.setStatusCode(Status.FAILED);
        newStatus.setMessage(message);
        newStatus.setException(exception);
        this.task.setStatus(newStatus);
    }

    private void cleanup() throws Exception {
        this.gramJob.removeListener(this);
        if (logger.isDebugEnabled()) {
            logger.debug("Destroying remote service for task "
                    + this.task.getIdentity().toString());
        }
        gramJob.destroy();
    }

    private Authorization getAuthorization(
            GlobusSecurityContextImpl securityContext) {
        Authorization authorization = HostAuthorization.getInstance();

        org.globus.gsi.gssapi.auth.Authorization auth = securityContext
                .getAuthorization();
        // map to GT4 authorization
        if (auth instanceof org.globus.gsi.gssapi.auth.SelfAuthorization) {
            authorization = SelfAuthorization.getInstance();
        }
        else if (auth instanceof org.globus.gsi.gssapi.auth.HostAuthorization) {
            authorization = HostAuthorization.getInstance();
        }
        return authorization;
    }

    /** Takes walltime of the form mm or hh:mm or hh:mm:ss and returns the
        number of minutes, rounding up the seconds. */
    public static Long wallTimeToMinutes(Object time) {
	long n;
        String[] s = time.toString().split(":");
        try {
            if (s.length == 1) {
                n = Integer.parseInt(s[0]);
            }
            else if (s.length == 2) {
                n = Integer.parseInt(s[1]) + 60 * Integer.parseInt(s[0]);     
            } else if (s.length == 3) {
                n = Integer.parseInt(s[1]) + 60 * Integer.parseInt(s[0]) + 1;
            }
            else {
                throw new IllegalArgumentException("Invalid time specification: " + time);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid time specification: " + time);
        }
        return new Long(n);
    }

}
