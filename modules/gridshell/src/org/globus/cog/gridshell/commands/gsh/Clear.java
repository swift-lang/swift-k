/*
 * Created on Mar 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.globus.cog.gridshell.commands.gsh;

import java.beans.PropertyChangeEvent;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.gridshell.commands.AbstractShellCommand;
import org.globus.cog.gridshell.getopt.app.GetOptImpl;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.interfaces.GridShell;
import org.globus.cog.gridshell.interfaces.Scope;

/**
 * 
 */
public class Clear extends AbstractShellCommand {
	private static final Logger logger = Logger.getLogger(Clear.class);
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#execute()
	 */
	public Object execute() throws Exception {
		logger.info("execute");
		GridShell gsh = getGridShell();
		if(gsh != null) {
			gsh.getGUI().setHistoryValue("");
			// do not need to set result
			this.setStatusCode(Status.COMPLETED);
			logger.info("command completed");
		}else {
			logger.info("commaned failed");
			this.setStatusFailed("Can't clear history, parent is not of type GridShell");
		}
		
		return null;
	}	

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#destroy()
	 */
	public Object destroy() throws Exception {
		// do nothing method
		return null;
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent arg0) {
		// do nothing method		
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.commands.AbstractShellCommand#getGetOpt()
	 */
	public GetOpt createGetOpt(Scope scope) {
		GetOpt result = new GetOptImpl(scope);
		
		return result;
	}

}
