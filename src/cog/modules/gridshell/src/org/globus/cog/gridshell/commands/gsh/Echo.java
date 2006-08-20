/*
 * 
 */
package org.globus.cog.gridshell.commands.gsh;

import java.beans.PropertyChangeEvent;

import org.globus.cog.gridshell.commands.AbstractShellCommand;
import org.globus.cog.gridshell.getopt.app.ArgumentImpl;
import org.globus.cog.gridshell.getopt.app.GetOptImpl;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.interfaces.Scope;

/**
 * 
 */
public class Echo extends AbstractShellCommand {

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.commands.AbstractShellCommand#createGetOpt()
	 */
	public GetOpt createGetOpt(Scope scope) {
		GetOpt result = new GetOptImpl(scope);
		result.isAllowDynamicArgs(false);
		result.addArgument(new ArgumentImpl("the value to echo",String.class,true));
		return result;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#execute()
	 */
	public Object execute() throws Exception {
		setResult(getopt.getArgumentAt(1).getValue());
		this.setStatusCompleted();
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
		// TODO Auto-generated method stub
		
	}
	
}
