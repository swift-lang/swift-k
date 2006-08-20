/*
 * 
 */
package org.globus.cog.gridshell.tasks;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;

/**
 * A task to remove a file
 * 
 */
public class RmFileTask extends AbstractFileOperationTask {
	/**
	 * Removes a file
	 * @param connection
	 * @param file
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public RmFileTask(StartTask connection, String file) throws InvalidProviderException, ProviderMethodException {
		super(connection, new String[] {file} );
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getOperation()
	 */
	public String getOperation() {
		return FileOperationSpecification.RMFILE;
	}

}
