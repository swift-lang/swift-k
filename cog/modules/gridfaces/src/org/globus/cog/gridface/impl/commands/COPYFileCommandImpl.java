
//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.gridface.impl.commands;

import java.io.File;

import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphImpl;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskGraph;
import org.globus.cog.gridface.interfaces.GridCommand;

public class COPYFileCommandImpl extends GridCommandImpl implements GridCommand {

	private TaskGraph taskgraph = null;
	private File tempFile = null;
	
	
	private GridCommand putCommand = new PUTFILECommandImpl();
	private GridCommand getCommand = new GETFILECommandImpl();
	
	
	public COPYFileCommandImpl() {
		super();
		setCommand("copyfile");
	}

	/*
	 * prepares the corresponding file operation task
	 */
	public ExecutableObject prepareTask() throws Exception {
		if (validate() == true) {
			this.taskgraph = new TaskGraphImpl();
			this.tempFile = File.createTempFile("temp", "file");
			Task getTask = prepareGetTask();
			Task putTask = preparePutTask();
			taskgraph.add(getTask);
			taskgraph.add(putTask);
			//CoG 4 changes
//			taskgraph.addDependency(
//				getTask.getIdentity(),
//				putTask.getIdentity());
			taskgraph.addDependency(getTask,putTask);
			return taskgraph;
		} else {
			return null;
		}
	}

	public boolean validate() {

		if ((getAttribute("sourceSessionId") == null)
			|| (getAttribute("sourceProvider") == null)
			|| (getAttribute("destinationProvider") == null)
			|| (getAttribute("destinationSessionId") == null))
			return false;
		else
			return true;
	}

	public Object getOutput() {
		if (getStatus().getStatusCode() == Status.COMPLETED) {
			return taskgraph.getCompletedTime();
		} else
			return null;
	}

	private Task prepareGetTask() throws Exception {
		
		getCommand.setAttribute("provider", getAttribute("sourceProvider"));
		getCommand.setAttribute("sessionId", getAttribute("sourceSessionId"));
		
		getCommand.addArgument(this.getArgument(0));
		getCommand.addArgument(tempFile.getAbsolutePath());
		return (Task) getCommand.prepareTask();
	}

	private Task preparePutTask() throws Exception {
		putCommand.setAttribute(
				"provider",
				getAttribute("destinationProvider"));
		putCommand.setAttribute(
			"sessionId",
			getAttribute("destinationSessionId"));

		
		putCommand.addArgument(tempFile.getAbsolutePath());
		putCommand.addArgument(this.getArgument(1));
		return (Task) putCommand.prepareTask();
	}
	
 public void addStatusListener(StatusListener listener) {
     putCommand.addStatusListener(listener);
 }
 
 public Identity getIdentity() {
     return putCommand.getIdentity();
 }

}
