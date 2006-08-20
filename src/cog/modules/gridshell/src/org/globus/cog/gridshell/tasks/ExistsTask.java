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
public class ExistsTask extends AbstractFileOperationTask {
	/**
	 * Determines if a file or directory exists
	 * @param connection
	 * @param file
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public ExistsTask(StartTask connection, String file) 
			throws InvalidProviderException, ProviderMethodException {
		super(connection, new String[] {file} );
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getOperation()
	 */
	public String getOperation() {
		return FileOperationSpecification.EXISTS;
	}

}
