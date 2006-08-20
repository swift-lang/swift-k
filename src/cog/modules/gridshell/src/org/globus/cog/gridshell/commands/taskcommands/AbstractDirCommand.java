/*
 * 
 */
package org.globus.cog.gridshell.commands.taskcommands;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.gridshell.getopt.app.ArgumentImpl;
import org.globus.cog.gridshell.getopt.app.GetOptImpl;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.tasks.AbstractFileOperationTask;
import org.globus.cog.gridshell.tasks.StartTask;

/**
 * An abstract command that supports tasks with directory as argument
 * 
 */
public abstract class AbstractDirCommand extends AbstractTaskCommand {
	/**
	 * Creates the "dir" task associated with the command
	 * @param connection
	 * @param dir
	 * @return
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public abstract AbstractFileOperationTask createDirTask(StartTask connection,String dir)  
			throws InvalidProviderException, ProviderMethodException;
	
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.commands.AbstractShellCommand#createGetOpt(org.globus.cog.gridface.impl.gridshell.interfaces.Scope)
	 */
	public GetOpt createGetOpt(Scope scope) {
		GetOpt result = new GetOptImpl(scope);
		result.addArgument(new ArgumentImpl("directory name",String.class,true));
		return result;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#execute()
	 */
	public Object execute() throws Exception {
		StartTask connection = getConnection();
		
		// extract from commandline
		String dir = (String)getGetOpt().getArgumentAt(1).getValue();
		AbstractFileOperationTask operation = createDirTask(connection,dir);
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
