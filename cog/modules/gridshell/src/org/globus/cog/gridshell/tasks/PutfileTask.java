/*
 * 
 */
package org.globus.cog.gridshell.tasks;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;

/**
 * A task that puts a file on a remote machine
 * 
 */
public class PutfileTask extends AbstractFileOperationTask {
	/**
	 * Create a task that puts a file on a remote machine
	 * @param connection
	 * @param sourceFile - the file on the local machine to send
	 * @param destinationFile - the location to place the file remotely
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public PutfileTask(StartTask connection, String sourceFile, String destinationFile) 
			throws InvalidProviderException, ProviderMethodException {
		super(connection, new String[] {sourceFile, destinationFile} );
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getOperation()
	 */
	public String getOperation() {
		return FileOperationSpecification.PUTFILE;
	}

}
