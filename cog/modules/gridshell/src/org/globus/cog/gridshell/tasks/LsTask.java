/*
 * 
 */
package org.globus.cog.gridshell.tasks;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;

/**
 * Creates a task that list the contents of a directory
 * 
 */
public class LsTask extends AbstractFileOperationTask {
	/**
	 * A task that lists the current directory
	 * @param connection
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public LsTask(StartTask connection) throws InvalidProviderException, ProviderMethodException {
		super(connection,null);
	}
	/**
	 * Creates a task to list a directories contents
	 * @param connection
	 * @param dir
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public LsTask(StartTask connection, String dir) throws InvalidProviderException, ProviderMethodException {
		super(connection, new String[] {dir});
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.open.tasks.AbstractFileTask#getOperation()
	 */
	public String getOperation() {
		return FileOperationSpecification.LS;
	}

}
