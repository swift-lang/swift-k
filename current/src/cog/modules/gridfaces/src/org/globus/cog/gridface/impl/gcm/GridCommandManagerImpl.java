
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.gcm;

// gvl: this component clearly does not do what was proposed in the
// specifiation. At this time it only relays the commands to the
// internals of the cog kit and does not do much more. however. this
// component was supposed to be doing checkpointing, allow 100.000
// grid commands to be run, and be able to still function even if the
// connection to the grid is lost. this includes fault tolerant
// mechanisms to restart. aslo the commands should make use of
// dependencies, such that the kill of a parent kills als the
// children. display of the "gridpstree should be simplified.

// even if this routine gets replaced by cor ore karajan, it is
// important to address the above requirements.

// essentially we need several ques to manage this. they should be
// checkpointable to a file or database.

// one of the ques contains all the jobs that have been prepared but
// are not yet submitted to the grid. these are pending jobs. abstraction and
// karajan have constructs for this also. at the time of instantiation
// they should not have listeners as this is too much overhead.

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.log4j.Logger;

import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphHandlerImpl;
import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphImpl;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.TaskGraph;
import org.globus.cog.abstraction.interfaces.TaskGraphHandler;
import org.globus.cog.abstraction.xml.TaskGraphMarshaller;
import org.globus.cog.abstraction.xml.TaskGraphUnmarshaller;
import org.globus.cog.gridface.interfaces.Scheduler;
import org.globus.cog.gridface.impl.desktop.interfaces.*;
import org.globus.cog.gridface.interfaces.GridCommand;
import org.globus.cog.gridface.interfaces.GridCommandManager;
/**
 * GridCommandManager accepts commands from GridFace GUIs and 
 * executes tasks for each command
 */
public class GridCommandManagerImpl implements GridCommandManager {
	
	private CoGTop desktop = null;
	
	private TaskGraph taskgraph;
	private TaskGraphHandler taskgraphHandler;
	private Hashtable identities = null;
	static Logger logger =
		Logger.getLogger(GridCommandManagerImpl.class.getName());

	public GridCommandManagerImpl() throws Exception {
		this.taskgraph = new TaskGraphImpl();
		this.taskgraphHandler = new TaskGraphHandlerImpl();
		this.taskgraphHandler.submit(this.taskgraph);
		identities = new Hashtable();
		taskgraphHandler.setTaskHandlerPolicy(TaskGraphHandler.CASCADED_TASK_HANDLER);
	}

	public GridCommandManagerImpl(File xmlFile) throws Exception {
		try {
			//File xmlFile = new File(fileName);
			this.taskgraph = (TaskGraph) TaskGraphUnmarshaller.unmarshal(xmlFile);
		} catch (Exception e) {
			logger.debug("Cannot load GCM from XML", e);
			throw new Exception("Cannot load GCM from XML", e);
		}
		this.taskgraphHandler = new TaskGraphHandlerImpl();
		this.taskgraphHandler.submit(this.taskgraph);
		identities = new Hashtable();
		taskgraphHandler.setTaskHandlerPolicy(TaskGraphHandler.CASCADED_TASK_HANDLER);
	}

	/**
	 * Obtain task from command, add dependancies and submit 
	 */
	public Identity execute(
		GridCommand command,
		boolean inBackground,
		Enumeration idList)
		throws Exception {
		//TODO to merge into CoG 4.. set aside for now
//		ExecutableObject executable = command.prepareTask();
//
//		while (idList.hasMoreElements()) {
//			addDependency(
//				(Identity) idList.nextElement(),
//				executable.getIdentity());
//		}
//		if (executable != null) {
//			if (inBackground == false) {
//				submit(executable);
//			} else {
//				CommandRunner runner = new CommandRunner(executable, this);
//				runner.start();
//			}
//		} else {
//			throw new Exception("Task submission failed");
//		}
//		identities.put(
//			executable.getIdentity().toString(),
//			executable.getIdentity());
//		return executable.getIdentity();
		return null;
	}

	/**
	 * Obtain task from command and submit 
	 */
	public Identity execute(GridCommand command, boolean inBackground)
		throws Exception {
		
		// added because sometimes there is no desktop
		if(this.desktop != null) {
		  command.addStatusListener(this.desktop);
		}
		
		ExecutableObject executable = command.prepareTask();
		if (executable != null) {
			if (inBackground == false) {
				submit(executable);
			} else {
				CommandRunner runner = new CommandRunner(executable, this);
				runner.start();
			}
		} else {
			throw new Exception("Task submission failed. Executable null");
		}
		identities.put(
			executable.getIdentity().toString(),
			executable.getIdentity());
		return executable.getIdentity();
	}

	public synchronized void submit(ExecutableObject executable) throws Exception {
		logger.info("submit");
		try {
			taskgraph.add(executable);
		} catch (Exception e) {			
			if (executable.getStatus().getStatusCode() != Status.FAILED) {
				executable.setStatus(Status.FAILED);
				executable.getStatus().setException(e);
			}
			throw e;
		}
	}

	public Status getStatus(String id) {
		return getStatus(getIdentityFromString(id));
	}
	/**
	 * get status of the given command
	 */
	public Status getStatus(Identity id) {
		if (taskgraph.contains(id) == true) {
			ExecutableObject eo = taskgraph.get(id);
			return eo.getStatus();
		}
		return null;
	}

	public CoGTop getDesktop() {
		return this.desktop;
	}
	public void setDesktop(CoGTop desktop) {
		this.desktop = desktop;

	}
	/**
	 * Add dependancy between commands
	 */
	private void addDependency(ExecutableObject from, ExecutableObject to) {
		taskgraph.addDependency(from, to);
	}

	public boolean cancel(String idString) throws Exception {
		return cancel(getIdentityFromString(idString));
	}

	/**
	 * cancel the command with given id. Returns true if command has been canceled sucessfully.
	 */
	public boolean cancel(Identity id) throws Exception {
		if (taskgraph.get(id) == null) {
			throw new Exception("ExecutableObject with given id does not exist");
		}
//		if (getStatus(id).getStatusCode() < 2
//			|| getStatus(id).getStatusCode() > 4) {
//			throw new Exception("ExecutableObject for given id is not active");
//		}
		return taskgraphHandler.cancel(id);
	}

	public boolean suspend(Identity id) throws Exception{
		if (taskgraph.get(id) == null) {
			throw new Exception("ExecutableObject with given id does not exist");
		}
		return taskgraphHandler.suspend(id);
	}
	public boolean resume(Identity id) throws Exception{
		if (taskgraph.get(id) == null) {
			throw new Exception("ExecutableObject with given id does not exist");
		}
		return taskgraphHandler.resume(id);
	}
	/** Return a list of all commands submitted to GCM */
	public Enumeration getAllCommands() {
		return taskgraphHandler.getGraph().elements();
	}

	/** Return a list of all commands with status as UNSUBMITTED */
	public Enumeration getUnsubmittedCommands() {
		return taskgraphHandler.getUnsubmittedNodes();
	}

	/** Return a list of all commands with status as SUBMITTED */
	public Enumeration getSubmittedCommands() {
		return taskgraphHandler.getSubmittedNodes();
	}

	/** Return a list of all commands with status as ACTIVE */
	public Enumeration getActiveCommands() {
		return taskgraphHandler.getActiveNodes();
	}

	/** Return a list of all commands with status as FAILED */
	public Enumeration getFailedCommands() {
		return taskgraphHandler.getFailedNodes();
	}

	/** Return a list of all commands with status as COMPLETED */
	public Enumeration getCompletedCommands() {
		return taskgraphHandler.getCompletedNodes();
	}

	/** Return a list of all commands with status as SUSPENDED */
	public Enumeration getSuspendedCommands() {
		return taskgraphHandler.getSuspendedNodes();
	}

	/** Return a list of all commands with status as CANCELED */
	public Enumeration getCanceledCommands() {
		return taskgraphHandler.getCanceledNodes();
	}

	/** set priority to execute the given command */
	public void setPriorities(long id, int priority) {
		//to be implemented
	}

	/** assign a scheduler for executin jobs submitted to gcm */
	public void setScheduler(Scheduler scheduler) {
		//to be implemented
	}

	/** Return scheduler used by GCM. Default scheduler is FIFO */
	public Scheduler getScheduler() {
		return null; //to be implemented
	}

	/** Save the state of GCM into an XML file */
	public void toXML(String fileName) throws Exception {
		try {
			//		Create a File to marshal to
			File xmlFile = new File(fileName);
			//		Marshal the task graph object
			TaskGraphMarshaller.marshal(this.taskgraph, xmlFile);
		} catch (Exception e) {
			logger.error("Cannot checkpoint the GCM", e);
			throw new Exception("Cannot checkpoint the GCM ", e);
		}
	}

	/** Add status listener to changes in GCM's taskgraph */
	public void addStatusListener(StatusListener listener) {
		taskgraph.addStatusListener(listener);
	}

	/** Remove status listener for GCM */
	public void removeStatusListener(StatusListener listener) {
		taskgraph.removeStatusListener(listener);
	}

	private Identity getIdentityFromString(String idString) {
		return (Identity) identities.get(idString);
	}

	//		int schemeIndex = idString.indexOf(':');
	//		int nameSpaceIndex = idString.indexOf('-', schemeIndex);
	//		String nameSpace = idString.substring(schemeIndex + 1, nameSpaceIndex);
	//		long value =
	//			Long.parseLong(
	//				idString.substring(nameSpaceIndex + 1, idString.length()));
	//		Identity newId = new IdentityImpl(nameSpace);
	//		newId.setValue(value);
	//		return newId;
}
