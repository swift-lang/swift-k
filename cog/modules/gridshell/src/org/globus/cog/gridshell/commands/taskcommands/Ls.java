/*
 * 
 */
package org.globus.cog.gridshell.commands.taskcommands;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.gridshell.getopt.app.ArgumentImpl;
import org.globus.cog.gridshell.getopt.app.GetOptImpl;
import org.globus.cog.gridshell.getopt.app.OptionImpl;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.tasks.AbstractFileOperationTask;
import org.globus.cog.gridshell.tasks.LsTask;
import org.globus.cog.gridshell.tasks.StartTask;

/**
 * 
 */
public class Ls extends AbstractTaskCommand {
	private static final Logger logger = Logger.getLogger(Ls.class);
	
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.commands.AbstractShellCommand#createGetOpt(org.globus.cog.gridface.impl.gridshell.interfaces.Scope)
	 */
	public GetOpt createGetOpt(Scope scope) {
		GetOpt result = new GetOptImpl(scope);
		result.addArgument(new ArgumentImpl("the directory",String.class,false));
		result.addOption(OptionImpl.createFlag("give details","l","long"));
		return result;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#execute()
	 */
	public Object execute() throws Exception {
		StartTask connection = getConnection();
		
		AbstractFileOperationTask operation;
		if(getGetOpt().getArgumentAt(1).isSet()) {			
			// extract from commandline
			String dir = (String)getGetOpt().getArgumentAt(1).getValue();
			logger.debug("specified a directory="+dir);
			// create the ls command
			operation = new LsTask(connection,dir);				
		}else {
			operation = new LsTask(connection);	
		}
		setTask(operation);		
								
		return super.execute();
	}


	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.taskcommands.AbstractTaskCommand#getTaskOutput()
	 */
	public Object getTaskOutput() {
		Collection dirContents = (Collection)getTask().getAttribute("output");
		StringBuffer result = new StringBuffer();
		Iterator iDirContents = dirContents.iterator();
		while(iDirContents.hasNext()) {
			GridFile value = (GridFile)iDirContents.next();
			if(getGetOpt().isOptionSet("l")) {
				result.append(value.getFileType());
				result.append(value.getUserPermissions());
				result.append(value.getGroupPermissions());
				result.append(value.getAllPermissions());
				result.append(value.getLastModified());
				result.append("\t");
			}
			result.append(value.getName());
			result.append("\n");
		}
		return result.toString();
	}
}
