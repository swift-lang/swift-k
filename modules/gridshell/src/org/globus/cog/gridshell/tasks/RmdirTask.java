/*
 * 
 */
package org.globus.cog.gridshell.tasks;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;

/**
 * A task to remove a directory
 * 
 */
public class RmdirTask extends AbstractFileOperationTask {

	/**
	 * Creates a task to remove a directory
	 * @param connection
	 * @param directory
	 * @param isForce
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public RmdirTask(StartTask connection, String directory, boolean isForce) throws InvalidProviderException, ProviderMethodException {
		super(connection, new String[] {directory, String.valueOf(isForce)} );
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getOperation()
	 */
	public String getOperation() {
		return FileOperationSpecification.RMDIR;
	}

}
