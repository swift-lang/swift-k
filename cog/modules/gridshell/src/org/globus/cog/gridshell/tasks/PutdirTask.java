/*
 * 
 */
package org.globus.cog.gridshell.tasks;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;

/**
 * A task that puts a directory on a remote machine
 * 
 */
public class PutdirTask extends AbstractFileOperationTask {
	/**
	 * Creates a task that puts a directory on a remote machine
	 * @param connection
	 * @param sourceDir - the directory on the local machine to send
	 * @param destinationDir - the location to place the directory remotely
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public PutdirTask(StartTask connection, String sourceDir, String destinationDir) 
			throws InvalidProviderException, ProviderMethodException {
		super(connection, new String[] {sourceDir, destinationDir} );
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getOperation()
	 */
	public String getOperation() {
		return FileOperationSpecification.PUTDIR;
	}

}
