/*
 * 
 */
package org.globus.cog.gridshell.tasks;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;

/**
 * A task that determines if something is a directory
 * 
 */
public class IsDirectoryTask extends AbstractFileOperationTask {
	/**
	 * Crates a task that determines if something is a directory
	 * 
	 * @param connection
	 * @param name
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public IsDirectoryTask(StartTask connection, String name) 
			throws InvalidProviderException, ProviderMethodException {
		super(connection, new String[] {name} );
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getOperation()
	 */
	public String getOperation() {
		return FileOperationSpecification.ISDIRECTORY;
	}

}
