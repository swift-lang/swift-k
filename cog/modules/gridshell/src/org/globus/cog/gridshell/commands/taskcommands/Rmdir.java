/*
 * 
 */
package org.globus.cog.gridshell.commands.taskcommands;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.gridshell.tasks.AbstractFileOperationTask;
import org.globus.cog.gridshell.tasks.RmdirTask;
import org.globus.cog.gridshell.tasks.StartTask;
/**
 * 
 */
public class Rmdir extends AbstractDirCommand {
	/*(non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.taskcommands.AbstractDirCommand#createDirCommand(org.globus.cog.gridshell.commands.taskcommand.tasks.StartTask, java.lang.String)
	 */
	public AbstractFileOperationTask createDirTask(StartTask connection, String dir) throws InvalidProviderException, ProviderMethodException {		
		return new RmdirTask(connection,dir,true);
	}
}
