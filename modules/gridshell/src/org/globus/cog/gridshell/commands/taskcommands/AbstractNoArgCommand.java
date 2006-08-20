/*
 * 
 */
package org.globus.cog.gridshell.commands.taskcommands;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.gridshell.getopt.app.GetOptImpl;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.tasks.AbstractFileOperationTask;
import org.globus.cog.gridshell.tasks.StartTask;

/**
 * 
 */
public abstract class AbstractNoArgCommand extends AbstractTaskCommand {
	
	public abstract AbstractFileOperationTask createNoArgCommand(StartTask connection)  throws InvalidProviderException, ProviderMethodException;
	
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.commands.AbstractShellCommand#createGetOpt(org.globus.cog.gridface.impl.gridshell.interfaces.Scope)
	 */
	public GetOpt createGetOpt(Scope scope) {
		GetOpt result = new GetOptImpl(scope);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#execute()
	 */
	public Object execute() throws Exception {
		StartTask connection = getConnection();
				
		AbstractFileOperationTask operation = createNoArgCommand(connection);
		setTask(operation);		
								
		return super.execute();
	}


	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.taskcommands.AbstractTaskCommand#getTaskOutput()
	 */
	public Object getTaskOutput() {		
		return getTask().getAttribute("output");
	}
}
