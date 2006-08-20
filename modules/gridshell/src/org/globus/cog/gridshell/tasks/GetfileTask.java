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
public class GetfileTask extends AbstractFileOperationTask {
	/**
	 * Gets a file
	 * @param connection
	 * @param sourceFile - the file on the server to get
	 * @param destinationFile - the location to place the file locally
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public GetfileTask(StartTask connection, String sourceFile, String destinationFile) 
			throws InvalidProviderException, ProviderMethodException {
		super(connection, new String[] {sourceFile, destinationFile} );
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getOperation()
	 */
	public String getOperation() {
		return FileOperationSpecification.GETFILE;
	}

}
