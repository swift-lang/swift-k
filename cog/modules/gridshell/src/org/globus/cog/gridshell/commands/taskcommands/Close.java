/*
 * 
 */
package org.globus.cog.gridshell.commands.taskcommands;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.gridshell.commands.CommandException;
import org.globus.cog.gridshell.tasks.AbstractFileOperationTask;
import org.globus.cog.gridshell.tasks.StartTask;
import org.globus.cog.gridshell.tasks.StopTask;
/**
 * 
 */
public class Close extends AbstractNoArgCommand {
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.taskcommands.AbstractNoArgCommand#createNoArgCommand(org.globus.cog.gridshell.commands.taskcommand.tasks.StartTask)
	 */
	public AbstractFileOperationTask createNoArgCommand(StartTask connection) throws InvalidProviderException, ProviderMethodException {
	    if(!getConnectionManager().isCurrentDefaultConnection()) {
	        getConnectionManager().pop();
			return new StopTask(connection);
	    }else {
	        throw new CommandException("Error: can't close default connection",getStatus());
	    }
	}
}
