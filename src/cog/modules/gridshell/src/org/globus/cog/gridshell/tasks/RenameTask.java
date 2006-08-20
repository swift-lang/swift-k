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
public class RenameTask extends AbstractFileOperationTask {
	/**
	 * Rename a file
	 * @param connection
	 * @param sourceFile - the files orgional name
	 * @param destinationFile - the new name of the file
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public RenameTask(StartTask connection, String orgFileName, String newFileName) 
			throws InvalidProviderException, ProviderMethodException {
		super(connection, new String[] {orgFileName, newFileName} );
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getOperation()
	 */
	public String getOperation() {
		return FileOperationSpecification.RENAME;
	}

}
