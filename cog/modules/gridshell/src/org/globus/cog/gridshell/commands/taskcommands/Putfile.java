/*
 * 
 */
package org.globus.cog.gridshell.commands.taskcommands;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.gridshell.tasks.AbstractFileOperationTask;
import org.globus.cog.gridshell.tasks.PutfileTask;
import org.globus.cog.gridshell.tasks.StartTask;
/**
 * 
 */
public class Putfile extends AbstractSrcDestinationCommand {
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.taskcommands.AbstractTransferCommand#createTransferCommand(org.globus.cog.gridshell.commands.taskcommand.tasks.StartTask, java.lang.String, java.lang.String)
	 */
	public AbstractFileOperationTask createSrcDestinationTask(StartTask connection, String source, String destination) 
			throws InvalidProviderException, ProviderMethodException {
		
		return new PutfileTask(connection,source,destination);
	}
}
