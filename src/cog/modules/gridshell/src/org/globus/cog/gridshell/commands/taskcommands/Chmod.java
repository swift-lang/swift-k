/*
 * 
 */
package org.globus.cog.gridshell.commands.taskcommands;

import org.globus.cog.gridshell.getopt.app.ArgumentImpl;
import org.globus.cog.gridshell.getopt.app.GetOptImpl;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.tasks.AbstractFileTask;
import org.globus.cog.gridshell.tasks.ChmodTask;
import org.globus.cog.gridshell.tasks.StartTask;

/**
 * A shell command for chmod
 * 
 * 
 */
public class Chmod extends AbstractTaskCommand {
	
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.commands.AbstractShellCommand#createGetOpt(org.globus.cog.gridface.impl.gridshell.interfaces.Scope)
	 */
	public GetOpt createGetOpt(Scope scope) {
		GetOpt result = new GetOptImpl(scope);
		result.addArgument(new ArgumentImpl("file name",String.class,true));
		result.addArgument(new ArgumentImpl("mode",String.class,true));
		return result;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#execute()
	 */
	public Object execute() throws Exception {
		// extract from commandline
		String file = (String)getGetOpt().getArgumentAt(1).getValue();
		String mode = (String)getGetOpt().getArgumentAt(1).getValue();	
		
		// Get the current connection
		StartTask connection = getConnection();
		
		// create the connection
		AbstractFileTask task = new ChmodTask(connection,file,mode);
		setTask(task);
								
		return super.execute();
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.taskcommands.AbstractTaskCommand#getTaskOutput()
	 */
	public Object getTaskOutput() {		
		return getTask().getAttribute("output");
	}
}
