/*
 * 
 */
package org.globus.cog.gridshell.tasks;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;

/**
 * A task to return the present working directory
 * 
 */
public class PwdTask extends AbstractFileOperationTask {

	/**
	 * Create a task to return the present working directory
	 * @param connection
	 * @param arguments
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public PwdTask(StartTask connection) throws InvalidProviderException, ProviderMethodException {
		super(connection, null);
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getOperation()
	 */
	public String getOperation() {
		return FileOperationSpecification.PWD;
	}

}
