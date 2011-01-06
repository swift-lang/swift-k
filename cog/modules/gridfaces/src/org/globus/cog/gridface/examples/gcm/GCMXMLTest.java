
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.examples.gcm;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.gridface.impl.commands.EXECCommandImpl;
import org.globus.cog.gridface.impl.gcm.GridCommandManagerImpl;
import org.globus.cog.gridface.interfaces.GridCommand;
import org.globus.cog.gridface.interfaces.GridCommandManager;

/**
 * this is a client application for the GridCommandManager. 
 * Tests GCM serialization. 
 */
public class GCMXMLTest implements StatusListener {

	private GridCommandManager gcm;
	static Logger logger = Logger.getLogger(GCMXMLTest.class.getName());
	private Identity id = null;
	public GCMXMLTest() throws Exception {
		gcm = new GridCommandManagerImpl();
	}

	public void executeTask() throws Exception {
		GridCommand command = new EXECCommandImpl();
		ServiceContact serviceContact =
			new ServiceContactImpl("arbat.mcs.anl.gov");
		command.setAttribute("servicecontact", serviceContact);
		command.setAttribute("provider", "GT2");
		command.setAttribute("executable", "/bin/date");
		command.setAttribute("taskArguments", "-i 5 -s 0");
		command.setAttribute("redirected", "true");
		command.addStatusListener(this);
		try {
			id = gcm.execute(command, false);
			logger.debug(id.toString());
		} catch (Exception e) {
			logger.debug(">>>Exception in gcmExecutionTest Exec<<<");
			e.printStackTrace();
		}
	}

	public void serialize(String fileName) throws Exception {
		gcm.toXML(fileName);
	}
	
	public void statusChanged(StatusEvent event) {
		Status status = event.getStatus();
		logger.debug("Command Status Changed to " + status.getStatusCode());
		GridCommand command = (GridCommand) event.getSource();
		if ((status.getStatusCode() == Status.COMPLETED)) {
			logger.debug(command.getOutput().toString());
		}
	}

	/*
	 * Main method
	 */
	public static void main(String[] args) {
		if (args.length ==0 || args[0].equals("-help")){
			logger.fatal("usage: ./gcm-xml-test {filename}");
			System.exit(1);
		} 
		String fileName = args[0];
		logger.debug("FileName: " + fileName);
		try {
			GCMXMLTest gcmTest = new GCMXMLTest();
			gcmTest.executeTask();
			gcmTest.serialize(fileName);
		} catch (Exception exception) {
			logger.error("Error in GCMXMLTest",exception);
		}
	}
}