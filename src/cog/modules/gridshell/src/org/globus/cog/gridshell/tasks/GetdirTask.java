/*
 * 
 */
package org.globus.cog.gridshell.tasks;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;

/**
 * 
 */
public class GetdirTask extends AbstractFileOperationTask {
	/**
	 * Gets a file
	 * @param connection
	 * @param sourceDir - the dir on the server to get
	 * @param destinationDir - the location to place the dir locally
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public GetdirTask(StartTask connection, String sourceDir, String destinationDir) 
			throws InvalidProviderException, ProviderMethodException {
		super(connection, new String[] {sourceDir, destinationDir} );
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getOperation()
	 */
	public String getOperation() {
		return FileOperationSpecification.GETDIR;
	}

}
