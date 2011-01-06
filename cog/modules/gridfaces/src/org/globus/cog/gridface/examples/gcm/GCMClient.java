
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.examples.gcm;

import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.gridface.impl.commands.CHDIRCommandImpl;
import org.globus.cog.gridface.impl.commands.EXECCommandImpl;
import org.globus.cog.gridface.impl.commands.LSCommandImpl;
import org.globus.cog.gridface.impl.commands.RENAMECommandImpl;
import org.globus.cog.gridface.impl.commands.RMDIRCommandImpl;
import org.globus.cog.gridface.impl.commands.URLCOPYCommandImpl;
import org.globus.cog.gridface.impl.gcm.GridCommandManagerImpl;
import org.globus.cog.gridface.interfaces.GridCommand;
import org.globus.cog.gridface.interfaces.GridCommandManager;

/**
 * this is a client application for the GridCommandManager. 
 * Tests job submission, file transfer and file operation tasks. 
 */
public class GCMClient implements StatusListener {
	//hi
	private GridCommandManager gcm;
	private Identity sessionId;
	private String username = "vijayaku";
	static Logger logger = Logger.getLogger(GCMClient.class.getName());
	public GCMClient() throws Exception {
		gcm = new GridCommandManagerImpl();
	}

	/*
	 * Connect to FTP/ Gridftp server
	 */
	public void executeOpen(String provider) throws Exception {
		//TODO put on hold for CoG 4 - GridFace integration, 
//		logger.debug(">>>Open connection<<<");
//		GridCommand command = new OPENCommandImpl();
//		command.setAttribute("provider", provider);
//
//		//If ftp, then need to submit username and password
//		if (provider.equalsIgnoreCase("ftp")) {
//			ServiceContact serviceContact = new ServiceContactImpl();
//			serviceContact.setIP("ftp.mcs.anl.gov");
//			serviceContact.setPort("21");
//			command.setAttribute("ServiceContact", serviceContact);
//
//			SecurityContext securityContext =
//				SecurityContextFactory.newSecurityContext(provider);
//			securityContext.setAttribute("username", "anonymous");
//			securityContext.setAttribute("password", "");
//			command.setAttribute("SecurityContext", securityContext);
//
//		} else if (
//			provider.equalsIgnoreCase("gsiftp")
//				|| provider.equalsIgnoreCase("gridftp")) {
//			ServiceContact serviceContact = new ServiceContactImpl();
//			serviceContact.setIP("arbat.mcs.anl.gov");
//			serviceContact.setPort("6224");
//			command.setAttribute("ServiceContact", serviceContact);
//
//			SecurityContext securityContext =
//				SecurityContextFactory.newSecurityContext(provider);
//			securityContext.setCredentials(null);
//			command.setAttribute("SecurityContext", securityContext);
//
//		} else if (provider.equalsIgnoreCase("http")) {
//			ServiceContact serviceContact = new ServiceContactImpl();
//			serviceContact.setURL("http://localhost:8080/slide");
//			command.setAttribute("ServiceContact", serviceContact);
//
//			SecurityContext securityContext =
//				SecurityContextFactory.newSecurityContext(provider);
//			securityContext.setAttribute("username", "root");
//			securityContext.setAttribute("password", "root");
//			command.setAttribute("SecurityContext", securityContext);
//		} else {
//			logger.error("Invalid provider specified");
//		}
//
//		try {
//			command.addStatusListener(this);
//			gcm.execute(command, false);
//		} catch (Exception e) {
//			logger.debug(">>>Exception in gcmClient open<<<");
//			e.printStackTrace();
//		}

	}

	/*
	 * LS command. Lists the contents of the directory
	 */
	public void executeLs(String provider) {
		logger.debug(">>>LS<<<");
		if (sessionId != null) {
			GridCommand command = new LSCommandImpl();
			command.setAttribute("provider", provider);
			command.setAttribute("sessionid", sessionId);
			command.addStatusListener(this);
			try {
				gcm.execute(command, true);
			} catch (Exception e) {
				logger.debug(">>>Exception in gcmClient LS<<<");
				e.printStackTrace();
			}
			logger.debug(">>>LS Successful<<<");
		} else {
			logger.debug(">>> No SessionId<<<");
		}
	}

	/*
	 * CHDIR command. Lists the contents of the directory
	 */
	public void executeCHDIR(String provider) {
		logger.debug(">>CHDIR<<<");
		if (sessionId != null) {
			GridCommand command = new CHDIRCommandImpl();
			command.setAttribute("provider", provider);
			command.setAttribute("sessionid", sessionId);
			command.addArgument(".");
			command.addStatusListener(this);
			try {
				gcm.execute(command, true);
			} catch (Exception e) {
				logger.debug(">>>Exception in gcmClient CHDIR<<<");
				e.printStackTrace();
			}
			logger.debug(">>>CHDIR Successful<<<");
		} else {
			logger.debug(">>> No SessionId<<<");
		}
	}

	/*
	 * RM command. Lists the contents of the directory
	 */
	public void executeRm(String provider) {
		logger.debug(">>>Rm<<<");
		if (sessionId != null) {
			GridCommand command = new RMDIRCommandImpl();
			command.setAttribute("provider", provider);
			command.setAttribute("sessionid", sessionId);
			command.addArgument("test");
			command.addArgument("true");
			command.addStatusListener(this);
			try {
				gcm.execute(command, false);
			} catch (Exception e) {
				logger.debug(">>>Exception in gcmClient RM<<<");
				e.printStackTrace();
			}
			logger.debug(">>>RM Successful<<<");
		} else {
			logger.debug(">>> No SessionId<<<");
		}
	}

	/*
		 * Rename command. Lists the contents of the directory
		 */
	public void executeRename(String provider) {
		logger.debug(">>>Rename<<<");
		if (sessionId != null) {
			GridCommand command = new RENAMECommandImpl();
			command.setAttribute("provider", provider);
			command.setAttribute("sessionid", sessionId);
			command.addArgument("hello");
			command.addArgument("hello2");
			command.addStatusListener(this);
			try {
				gcm.execute(command, false);
			} catch (Exception e) {
				logger.debug(">>>Exception in gcmClient Rename<<<");
				e.printStackTrace();
			}
			logger.debug(">>>Rename Successful<<<");
		} else {
			logger.debug(">>> No SessionId<<<");
		}
	}

	/*
	 * Exec command. Runs an executable
	 */
	public void executeExec() throws Exception {
		logger.debug(">>>EXEC<<<");
		GridCommand command = new EXECCommandImpl();

		ServiceContact serviceContact =
			new ServiceContactImpl("arbat.mcs.anl.gov");
		command.setAttribute("servicecontact", serviceContact);

		command.setAttribute("provider", "GT2");
		command.setAttribute("executable", "/bin/date");
		command.setAttribute("redirected", "true");
		command.addStatusListener(this);

		try {
			gcm.execute(command, true);
		} catch (Exception e) {
			logger.debug(">>>Exception in gcmClient Exec<<<");
			e.printStackTrace();
		}
		logger.debug(">>>Exec Successful<<<");
	}

	/*
	 * File Transfer URL copy task
	 */
	public void executeURLCOPY() {
		logger.debug(">>>FILE TRANSFER<<<");
		GridCommand command = new URLCOPYCommandImpl();
		command.setAttribute("provider", "GT2");
		command.setAttribute(
			"source",
			"gsiftp://arbat.mcs.anl.gov:2811//home/vijayaku/tempFile");
		command.setAttribute(
			"destination",
			"gsiftp://wiggum.mcs.anl.gov:6224//home/vijayaku/test/hello");
		command.addStatusListener(this);

		try {
			gcm.execute(command, false);
		} catch (Exception e) {
			logger.debug(">>>Exception in gcmClient File Transfer<<<");
			e.printStackTrace();
		}
		logger.debug(">>>File Transfer Successful<<<");

	}

	/* (non-Javadoc)
	 * @see org.globus.cog.abstraction.interfaces.StatusListener#statusChanged(org.globus.cog.abstraction.impl.common.StatusEvent)
	 */
	public void statusChanged(StatusEvent event) {
		Status status = event.getStatus();
		logger.debug("Command Status Changed to " + status.getStatusCode());
		GridCommand command = (GridCommand) event.getSource();
		if ((status.getStatusCode() == Status.COMPLETED)) {
			logger.debug(command.getCommand() + "Command Completed");
			if (command.getCommand().equals("open")) {
				//open returns sessionid for future reference
				sessionId = (Identity) command.getOutput();
				logger.debug(sessionId);
			} else if (command.getCommand().equals("list")) {
				// list return Enumeration
				Enumeration e = (Enumeration) command.getOutput();
				while (e.hasMoreElements()) {
					logger.debug(e.nextElement().toString());
				}
			} else {
				if (command.getOutput() != null)
					logger.debug(command.getOutput().toString());
			}
		}
	}

	/*
		 * Main method
		 */
	public static void main(String[] args) {
		try {
			GCMClient gcmClient = new GCMClient();
			String provider = "gridftp";
			gcmClient.executeOpen(provider);
			gcmClient.executeLs(provider);
			//          gcmClient.executeRename(provider);
			//          gcmClient.executeLs(provider);
			//          gcmClient.executeRm(provider);
			//          gcmClient.executeLs(provider);
			          gcmClient.executeCHDIR(provider);
						gcmClient.executeLs(provider);
			          gcmClient.executeExec();
			//          gcmClient.executeURLCOPY();
			//          gcmClient.executeCOPY("gridftp");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
