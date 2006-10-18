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
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.NonNegativeInteger;
import org.apache.axis.types.PositiveInteger;
import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.exec.client.GramJob;
import org.globus.exec.client.GramJobListener;
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
public class JobSubmissionTaskHandler implements DelegatedTaskHandler, GramJobListener {
	static Logger logger = Logger.getLogger(JobSubmissionTaskHandler.class.getName());
	private Task task = null;
	private GramJob gramJob;

	private static Map jmMappings;

	static {
		jmMappings = new HashMap();
		jmMappings.put("fork", "Fork");
		jmMappings.put("pbs", "PBS");
		jmMappings.put("lsf", "LSF");
		jmMappings.put("condor", "Condor");
	}

	public void submit(Task task) throws IllegalSpecException, InvalidSecurityContextException,
			InvalidServiceContactException, TaskSubmissionException {
		if (this.task != null) {
			throw new TaskSubmissionException(
					"JobSubmissionTaskHandler cannot handle two active jobs simultaneously");
		}
		else {
			this.task = task;

			JobDescriptionType rsl;
			JobSpecification spec;
			try {
				spec = (JobSpecification) this.task.getSpecification();
			}
			catch (Exception e) {
				throw new IllegalSpecException("Exception while retreiving Job Specification", e);
			}

			Service service = this.task.getService(0);
			ServiceContact serviceContact = service.getServiceContact();
			String server = serviceContact.getContact();

			String factoryType = ManagedJobFactoryConstants.DEFAULT_FACTORY_TYPE;
			if (service instanceof ExecutionService) {
				String jobManager = ((ExecutionService) service).getJobManager();
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
				factoryURL = ManagedJobFactoryClientHelper.getServiceURL(server).getURL();
                
                // Fix (hack) for the above returning the wrong default port
                if (server.indexOf(':') == -1) {
                    factoryURL = new URL(factoryURL.getProtocol(), factoryURL
                            .getHost(), 8443, factoryURL.getFile());
                }

				factoryEndpoint = ManagedJobFactoryClientHelper.getFactoryEndpoint(factoryURL,
						factoryType);
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

			GlobusSecurityContextImpl securityContext = (GlobusSecurityContextImpl) this.task.getService(
					0).getSecurityContext();

			try {
				this.gramJob.setCredentials((GSSCredential) securityContext.getCredentials());
			}
			catch (IllegalArgumentException iae) {
				throw new InvalidSecurityContextException("Cannot set the SecurityContext twice",
						iae);
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

			this.gramJob.setDelegationEnabled(spec.isDelegationEnabled());

			try {
				this.gramJob.submit(factoryEndpoint, spec.isBatchJob(), true, "uuid:"
						+ UUIDGenFactory.getUUIDGen().nextUUID());
				logger.info("Job submitted");
				if (spec.isBatchJob()) {
					this.task.setStatus(Status.COMPLETED);
				}
			}
			catch (Exception e) {
				Status newStatus = new StatusImpl();
				Status oldStatus = this.task.getStatus();
				newStatus.setPrevStatusCode(oldStatus.getStatusCode());
				newStatus.setStatusCode(Status.FAILED);
				newStatus.setException(e);
				this.task.setStatus(newStatus);
				cleanup();
				throw new TaskSubmissionException("Cannot submit job: " + e.getMessage(), e);
			}
		}
	}

	public void suspend() throws InvalidSecurityContextException, TaskSubmissionException {
		throw new UnsupportedOperationException("suspend");
	}

	public void resume() throws InvalidSecurityContextException, TaskSubmissionException {
		throw new UnsupportedOperationException("resume");
	}

	public void cancel() throws InvalidSecurityContextException, TaskSubmissionException {
		try {
			this.gramJob.cancel();
			this.task.setStatus(Status.CANCELED);
		}
		catch (Exception e) {
			throw new TaskSubmissionException("Cannot cancel job", e);
		}
		finally {
			cleanup();
		}
	}

	private JobDescriptionType prepareSpecification(JobSpecification spec, String server)
			throws IllegalSpecException, TaskSubmissionException {
		// if the job specification is explicitly specified
		JobDescriptionType desc = new JobDescriptionType();

		if (spec.getDirectory() != null) {
			desc.setDirectory(spec.getDirectory());
		}
		if (spec.getAttribute("count") != null) {
			desc.setCount(new PositiveInteger(spec.getAttribute("count").toString()));
		}
		if (spec.getStdInput() != null) {
			desc.setStdin(spec.getStdInput());
		}
		if (spec.getAttribute("maxCpuTime") != null) {
			desc.setMaxCpuTime(new Long(spec.getAttribute("maxCpuTime").toString()));
		}
		if (spec.getAttribute("hostCount") != null) {
			desc.setHostCount(new PositiveInteger(spec.getAttribute("hostCount").toString()));
		}
		if (spec.getAttribute("jobType") != null) {
			desc.setJobType(JobTypeEnumeration.fromString(spec.getAttribute("jobType").toString()));
		}
		if (spec.getAttribute("maxMemory") != null) {
			desc.setMaxMemory(new NonNegativeInteger(spec.getAttribute("maxMemory").toString()));
		}
		if (spec.getAttribute("maxTime") != null) {
			desc.setMaxTime(new Long(spec.getAttribute("maxTime").toString()));
		}
		if (spec.getAttribute("maxWallTime") != null) {
			desc.setMaxWallTime(new Long(spec.getAttribute("maxWallTime").toString()));
		}
		if (spec.getAttribute("minMemory") != null) {
			desc.setMinMemory(new NonNegativeInteger(spec.getAttribute("minMemory").toString()));
		}
		if (spec.getAttribute("project") != null) {
			desc.setProject((String) spec.getAttribute("project"));
		}
		if (spec.getAttribute("queue") != null) {
			desc.setQueue((String) spec.getAttribute("queue"));
		}

		desc.setArgument((String[]) spec.getArgumentsAsList().toArray(new String[0]));

		boolean batchJob = spec.isBatchJob();
		if (spec.isRedirected()) {
			throw new IllegalSpecException("The gt4.0.0 provider does not support redirection");
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

		if (spec.isLocalExecutable()) {
			throw new IllegalSpecException(
					"The gt4.0.0 provider does not support local executables");
		}
		desc.setExecutable(spec.getExecutable());

		Collection environment = spec.getEnvironmentVariableNames();
		if (environment != null && environment.size() > 0) {
			NameValuePairType[] envVars = new NameValuePairType[environment.size()];
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

	public void stateChanged(GramJob job) {
		StateEnumeration state = job.getState();
		if (state.equals(StateEnumeration.Active)) {
			this.task.setStatus(Status.ACTIVE);
		}
		else if (state.equals(StateEnumeration.Failed)) {
			Status newStatus = new StatusImpl();
			Status oldStatus = this.task.getStatus();
			newStatus.setPrevStatusCode(oldStatus.getStatusCode());
			newStatus.setStatusCode(Status.FAILED);
			int errorCode = job.getError();
			if (job.getFault() != null) {
				newStatus.setMessage("#" + errorCode + " " + job.getFault().getDescription()[0]);
				newStatus.setException((Exception) job.getFault().getCause());
			}
			else {
				newStatus.setMessage("#" + errorCode);
			}
			this.task.setStatus(newStatus);
			cleanup();
		}
		else if (state.equals(StateEnumeration.Done)) {
			this.task.setStatus(Status.COMPLETED);
			cleanup();
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
	}

	private void cleanup() {
		this.gramJob.removeListener(this);
		logger.debug("Destroying remote service for task " + this.task.getIdentity().toString());
		try {
			gramJob.release();
			this.gramJob.destroy();
		}
		catch (Exception e) {
			logger.warn("Unable to destroy remote service for task "
					+ this.task.getIdentity().toString(), e);
		}
	}

	private Authorization getAuthorization(GlobusSecurityContextImpl securityContext) {
		Authorization authorization = HostAuthorization.getInstance();

		org.globus.gsi.gssapi.auth.Authorization auth = securityContext.getAuthorization();
		// map to GT4 authorization
		if (auth instanceof org.globus.gsi.gssapi.auth.SelfAuthorization) {
			authorization = SelfAuthorization.getInstance();
		}
		else if (auth instanceof org.globus.gsi.gssapi.auth.HostAuthorization) {
			authorization = HostAuthorization.getInstance();
		}
		return authorization;
	}
}