/*
 * 
 */
package org.globus.cog.gridshell.commands.taskcommands;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.gridshell.tasks.AbstractFileOperationTask;
import org.globus.cog.gridshell.tasks.PwdTask;
import org.globus.cog.gridshell.tasks.StartTask;
/**
 * 
 */
public class Pwd extends AbstractNoArgCommand {
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.taskcommands.AbstractNoArgCommand#createNoArgCommand(org.globus.cog.gridshell.commands.taskcommand.tasks.StartTask)
	 */
	public AbstractFileOperationTask createNoArgCommand(StartTask connection) throws InvalidProviderException, ProviderMethodException {		
		return new PwdTask(connection);
	}
}
