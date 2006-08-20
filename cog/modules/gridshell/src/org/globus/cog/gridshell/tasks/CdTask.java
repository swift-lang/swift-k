/*
 * 
 */
package org.globus.cog.gridshell.tasks;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;

/**
 * A task taht changes the current directory
 * 
 */
public class CdTask extends AbstractFileOperationTask {
		
	/**
	 * Creates a task that changes to a new directory
	 * @param connection
	 * @param directory
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public CdTask(StartTask connection, String directory) throws InvalidProviderException, ProviderMethodException {
		super(connection, new String[] {directory});
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getOperation()
	 */
	public String getOperation() {
		return FileOperationSpecification.CD;
	}

}
