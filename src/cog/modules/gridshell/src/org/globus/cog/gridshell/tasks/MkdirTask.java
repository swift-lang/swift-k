/*
 * 
 */
package org.globus.cog.gridshell.tasks;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;

/**
 * A task taht makes a new directory
 * 
 */
public class MkdirTask extends AbstractFileOperationTask {

	/**
	 * Creates a task to make a new directory
	 * @param connection
	 * @param dir
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public MkdirTask(StartTask connection, String dir) throws InvalidProviderException, ProviderMethodException {
		super(connection, new String[] {dir});
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getOperation()
	 */
	public String getOperation() {
		return FileOperationSpecification.MKDIR;
	}

}
