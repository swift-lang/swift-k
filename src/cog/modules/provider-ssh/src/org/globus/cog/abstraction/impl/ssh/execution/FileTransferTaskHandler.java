// This program is distributed under the following licence:
// http://www.globus.org/toolkit/download/license.html

package org.globus.cog.abstraction.impl.ssh.execution;

import java.io.File;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractDelegatedTaskHandler;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.ssh.SSHChannel;
import org.globus.cog.abstraction.impl.ssh.SSHChannelManager;
import org.globus.cog.abstraction.impl.ssh.SSHRunner;
import org.globus.cog.abstraction.impl.ssh.SSHTaskStatusListener;
import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

public class FileTransferTaskHandler extends AbstractDelegatedTaskHandler implements SSHTaskStatusListener {
	static Logger logger = Logger.getLogger(FileTransferTaskHandler.class.getName());
	private Task task = null;
	private Sftp sftp;

	public void submit(Task task) throws IllegalSpecException, InvalidSecurityContextException,
			InvalidServiceContactException, TaskSubmissionException {
		checkAndSetTask(task);
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

		SSHChannel s = SSHChannelManager.getDefault().getChannel(contact.getHost(),
                contact.getPort(), service.getSecurityContext().getCredentials());
		
		
		SSHRunner r = new SSHRunner(s, sftp);
		r.addListener(this);
		r.startRun(sftp);
		task.setStatus(Status.ACTIVE);
	}

	public void suspend() throws InvalidSecurityContextException, TaskSubmissionException {
	}

	public void resume() throws InvalidSecurityContextException, TaskSubmissionException {
	}

	public void cancel(String message) throws InvalidSecurityContextException, TaskSubmissionException {
	    //TODO This gets the prize for inventively useless
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
		    failTask(null, e);
		}
		else {
			logger.warn("Unknown status code: " + status);
			return;
		}
		cleanup();
	}
}