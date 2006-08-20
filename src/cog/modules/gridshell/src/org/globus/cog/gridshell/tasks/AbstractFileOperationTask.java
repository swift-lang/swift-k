/*
 * 
 */
package org.globus.cog.gridshell.tasks;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

/**
 * This is a super class for any non-START file operation task. Allows information
 * for the task to be gotten from the START task.
 * 
 * 
 */
public abstract class AbstractFileOperationTask extends AbstractFileTask {
	private static Logger logger = Logger.getLogger(AbstractFileOperationTask.class);
	
	private StartTask connection;
	
	public AbstractFileOperationTask(StartTask connection, String[] arguments) 
			throws InvalidProviderException, ProviderMethodException {
		super(arguments);
		setConnection(connection);
	}
	
	private void setConnection(StartTask connection) {
		this.connection = connection;
	}
	
	public void initTask() throws InvalidProviderException, ProviderMethodException {
		super.initTask();
		
		// for non-start operations
		Task task = this;
		Identity sessionId = getSessionId();		
		logger.debug("sessionId="+sessionId);
		
		// set the sessionID
		task.setAttribute("sessionID", sessionId);
	}
	
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getProvider()
	 */
	public String getProvider() {		
		return connection.getProvider();
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getServiceContact()
	 */
	public String getServiceContact() {
		return connection.getServiceContact();
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getSessionId()
	 */
	public Identity getSessionId() {
		return connection.getSessionId();
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getTaskHandler()
	 */
	public TaskHandler getTaskHandler() {
		return connection.getTaskHandler();
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getPort()
	 */
	public int getPort() {
		return connection.getPort();
	}
}
