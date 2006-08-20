/*
 * 
 */
package org.globus.cog.gridshell.tasks;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;

/**
 * Stops, closes, the connection
 * 
 */
public class StopTask extends AbstractFileOperationTask {

	/**
	 * A task that stops the connection
	 * @param connection
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public StopTask(StartTask connection) throws InvalidProviderException, ProviderMethodException {
		super(connection, null);
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getOperation()
	 */
	public String getOperation() {
		return FileOperationSpecification.STOP;
	}

}
