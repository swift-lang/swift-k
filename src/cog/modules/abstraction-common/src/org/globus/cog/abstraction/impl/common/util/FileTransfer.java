package org.globus.cog.abstraction.impl.common.util;

import java.io.File;
import java.net.PasswordAuthentication;
import java.net.URI;

import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.PublicKeyAuthentication;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.FileTransferSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

/**
 * Contributed by Joe Futrelle (futrelle@ncsa.uiuc.edu)
 */
public class FileTransfer implements StatusListener {
	Object sourceCredentials = null;
	Object destCredentials = null;
	boolean isComplete = false;

	// security params

	public static Object newPublicKeyCredentials(String username, String keyfile, String passphrase) {
		return new PublicKeyAuthentication(username, keyfile, passphrase.toCharArray());
	}

	public static Object newUsernamePasswordCredentials(String username, String password) {
		return new PasswordAuthentication(username, password.toCharArray());
	}

	public void setSourceCredentials(String username, String password) {
		sourceCredentials = new PasswordAuthentication(username, password.toCharArray());
	}

	public void setSourceCredentials(String username, String keyfile, String password) {
		sourceCredentials = new PublicKeyAuthentication(username, keyfile, password.toCharArray());
	}

	public void setSourceCredentials(Object credentials) {
		destCredentials = credentials;
	}

	public void setDestinationCredentials(String username, String password) {
		destCredentials = new PasswordAuthentication(username, password.toCharArray());
	}

	public void setDestinationCredentials(String username, String keyfile, String password) {
		destCredentials = new PublicKeyAuthentication(username, keyfile, password.toCharArray());
	}

	public void setDestinationCredentials(Object credentials) {
		destCredentials = credentials;
	}

	// action method

	public Task transfer(String sourceUri, String destUri) throws Exception {
		return transfer(sourceUri, destUri, true);
	}

	public Task transfer(String sourceUri, String destUri, boolean block) throws Exception {
		FileTransferSpecificationImpl spec = new FileTransferSpecificationImpl();

		Service sourceService = new ServiceImpl();
		Service destService = new ServiceImpl();

		configureService(sourceService, sourceUri, sourceCredentials);
		configureService(destService, destUri, destCredentials);

		spec.setSourceDirectory(getDirectory(sourceUri));
		spec.setSourceFile(getFile(sourceUri));

		spec.setDestinationDirectory(getDirectory(destUri));
		spec.setDestinationFile(getFile(destUri));

		Task task = getTask(spec, sourceService, destService);

		TaskHandler handler = AbstractionFactory.newFileTransferTaskHandler();
		handler.submit(task);

		if (block) {
			task.addStatusListener(this);
			while (!isComplete()) {
				try {
					synchronized (this) {
						wait();
					}
				}
				catch (InterruptedException x) {
					// do nothing
				}
			}
		}

		return task;
	}

	Task getTask(FileTransferSpecificationImpl spec, Service source, Service dest) throws Exception {
		Task task = new TaskImpl();
		task.setType(Task.FILE_TRANSFER);
		task.setSpecification(spec);
		task.setService(Service.FILE_TRANSFER_SOURCE_SERVICE, source);
		task.setService(Service.FILE_TRANSFER_DESTINATION_SERVICE, dest);
		return task;
	}

	void configureService(Service service, String uri, Object credentials) throws Exception {
		SecurityContext securityContext = null;
		URI u = new URI(uri);
		String protocol = u.getScheme();
		if (protocol.equals("file")) {
			service.setProvider("local");
		}
		else {
			if (protocol.equals("gsiftp")) {
				service.setProvider("GridFTP");
				securityContext = AbstractionFactory.newSecurityContext("GridFTP");
			}
			else if (protocol.equals("scp")) {
				service.setProvider("ssh");
				securityContext = AbstractionFactory.newSecurityContext("ssh");
			}
			else {
				throw new Exception("unsupported protocol: " + protocol);
			}
			securityContext.setCredentials(credentials);
			service.setSecurityContext(securityContext);
		}
		String host = u.getHost();
		if (host == null) {
			host = "localhost";
		}
		service.setServiceContact(new ServiceContactImpl(host));
	}

	String getDirectory(String uri) throws Exception {
		String parent = (new File((new URI(uri)).getPath().substring(1))).getParent();
		return (parent == null) ? "" : parent;
	}

	String getFile(String uri) throws Exception {
		return (new File((new URI(uri)).getPath().substring(1))).getName();
	}

	synchronized void setIsComplete(boolean ic) {
		isComplete = ic;
		notifyAll();
	}

	boolean isComplete() {
		return isComplete;
	}

	public void statusChanged(StatusEvent event) {
		int sc = event.getStatus().getStatusCode();
		if (sc == Status.ACTIVE) {
		}
		else if (sc == Status.COMPLETED) {
			setIsComplete(true);
		}
		else if (sc == Status.CANCELED) {
			setIsComplete(true);
		}
		else if (sc == Status.FAILED) {
			setIsComplete(true);
		}
		else if (sc == Status.RESUMED) {
		}
		else if (sc == Status.SUBMITTED) {
		}
		else if (sc == Status.SUSPENDED) {
		}
		else if (sc == Status.UNKNOWN) {
		}
		else if (sc == Status.UNSUBMITTED) {
		}
		else {
		}
	}

	public static void main(String args[]) throws Exception {
		FileTransfer ft = new FileTransfer();
		ft.transfer("file:////tmp/fttest.txt", "gsiftp://chasm.ncsa.uiuc.edu/joejoejoe.txt");
		System.out.println("done uploading");
		ft.transfer("gsiftp://chasm.ncsa.uiuc.edu/joejoejoe.txt", "file:////tmp/joejoejoe.txt");
		System.out.println("done downloading");
	}
}