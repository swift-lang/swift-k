// This program is distributed under the following licence:
// http://www.globus.org/toolkit/download/license.html

package org.globus.cog.abstraction.impl.execution.ssh;

import java.io.File;
import java.net.PasswordAuthentication;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.PublicKeyAuthentication;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;
import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

public class FileTransferTaskHandler implements DelegatedTaskHandler, SSHTaskStatusListener {
	static Logger logger = Logger.getLogger(FileTransferTaskHandler.class.getName());
	private Task task = null;
	private Sftp sftp;

	public void submit(Task task) throws IllegalSpecException, InvalidSecurityContextException,
			InvalidServiceContactException, TaskSubmissionException {
		if (this.task != null) {
			throw new TaskSubmissionException(
					"FileTransferTaskHandler cannot handle two active transfers simultaneously");
		}
		else {
			this.task = task;
			FileTransferSpecification spec;
			try {
				spec = (FileTransferSpecification) this.task.getSpecification();
			}
			catch (Exception e) {
				throw new IllegalSpecException("Exception while retreiving File Specification", e);
			}
			sftp = new Sftp();
			Service sourceService = task.getService(Service.FILE_TRANSFER_SOURCE_SERVICE);
			Service destinationService = task.getService(Service.FILE_TRANSFER_DESTINATION_SERVICE);
			ServiceContact source = sourceService.getServiceContact();
			ServiceContact destination = destinationService.getServiceContact();
			if (!source.getHost().equals("localhost") && !destination.getHost().equals("localhost")) {
				throw new TaskSubmissionException(
						"The SSH handler does not support 3rd party transfers yet");
			}
			ServiceContact contact;
			Service service;
			if (destination.getHost().equals("localhost")) {
				sftp.setGet(makeURL(spec.getSourceDirectory(), spec.getSourceFile()));
				sftp.setDest(makeLocalURL(spec.getDestinationDirectory(), spec.getDestinationFile()));
				contact = source;
				service = sourceService;
			}
			else {
				sftp.setPut(makeLocalURL(spec.getSourceDirectory(), spec.getSourceFile()));
				sftp.setDest(makeURL(spec.getDestinationDirectory(), spec.getDestinationFile()));
				contact = destination;
				service = destinationService;
			}

			sftp.setHost(contact.getHost());
			sftp.setVerifyhost(false);
			if (contact.getPort() != -1) {
				sftp.setPort(contact.getPort());
			}
			sftp.setOutput(false);
			SecurityContext sec = service.getSecurityContext();
            if (sec.getCredentials() instanceof PasswordAuthentication) {
            	PasswordAuthentication auth = (PasswordAuthentication) sec.getCredentials();
            	sftp.setUsername(auth.getUserName());
            	sftp.setPassword(new String(auth.getPassword()));
            }
            else if (sec.getCredentials() instanceof PublicKeyAuthentication) {
            	PublicKeyAuthentication auth = (PublicKeyAuthentication) sec.getCredentials();
            	sftp.setUsername(auth.getUsername());
            	sftp.setPassphrase(new String(auth.getPassPhrase()));
            	sftp.setKeyfile(auth.getPrivateKeyFile().getAbsolutePath());
            }
            else {
            	throw new InvalidSecurityContextException("Unsupported credentials: "+sec.getCredentials());
            }
			
			SSHRunner sr = new SSHRunner(sftp);
			sr.addListener(this);
			sr.start();
			task.setStatus(Status.ACTIVE);
		}
	}

	public void suspend() throws InvalidSecurityContextException, TaskSubmissionException {
	}

	public void resume() throws InvalidSecurityContextException, TaskSubmissionException {
	}

	public void cancel() throws InvalidSecurityContextException, TaskSubmissionException {
		try {
		}
		catch (Exception e) {
			throw new TaskSubmissionException("Unable to cancel task", e);
		}
	}

	private String makeURL(String dir, String file) {
		if (dir == null || dir.equals("")) {
			return file;
		}
		else {
			return dir + "/" + file;
		}
	}

	private String makeLocalURL(String dir, String file) {
		if (dir == null || dir.equals("")) {
			dir = new File("").getAbsolutePath();
		}
		return makeURL(dir, file);
	}

	private void cleanup() {
		// perform cleanup if required
	}

	public void SSHTaskStatusChanged(int status, Exception e) {
		if (status == SSHTaskStatusListener.COMPLETED) {
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
}