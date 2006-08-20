/*
 * 
 */
package org.globus.cog.gridshell.tasks;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;

/**
 * A task that changes the file permissions
 * 
 */
public class ChmodTask extends AbstractFileOperationTask {
		
	/**
	 * Creates a task that changes the file permissions
	 * @param connection
	 * @param fileName
	 * @param mode
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public ChmodTask(StartTask connection, String fileName, String mode) 
			throws InvalidProviderException, ProviderMethodException {
		super(connection, new String[] {fileName, mode});
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getOperation()
	 */
	public String getOperation() {
		return FileOperationSpecification.CHMOD;
	}

}
