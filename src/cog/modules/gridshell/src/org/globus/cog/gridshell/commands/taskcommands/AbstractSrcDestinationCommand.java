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
 * An abstract class for shellcommands that have source and destination as argumetns
 * 
 */
public abstract class AbstractSrcDestinationCommand extends AbstractTaskCommand {
	/**
	 * Creates the specific task for this command
	 * @param connection
	 * @param source
	 * @param destination
	 * @return
	 * @throws InvalidProviderException
	 * @throws ProviderMethodException
	 */
	public abstract AbstractFileOperationTask createSrcDestinationTask(StartTask connection,String source,String destination)  
			throws InvalidProviderException, ProviderMethodException;
	
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.commands.AbstractShellCommand#createGetOpt(org.globus.cog.gridface.impl.gridshell.interfaces.Scope)
	 */
	public GetOpt createGetOpt(Scope scope) {
		GetOpt result = new GetOptImpl(scope);
		result.addArgument(new ArgumentImpl("source",String.class,true));
		result.addArgument(new ArgumentImpl("destination",String.class,true));
		return result;
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#execute()
	 */
	public Object execute() throws Exception {
		StartTask connection = getConnection();
		
		// extract from commandline
		String source = (String)getGetOpt().getArgumentAt(1).getValue();
		String destination = (String)getGetOpt().getArgumentAt(1).getValue();
		AbstractFileOperationTask operation = createSrcDestinationTask(connection,source,destination);
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