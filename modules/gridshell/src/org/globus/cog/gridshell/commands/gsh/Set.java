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
public class Set extends AbstractShellCommand {

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.commands.AbstractShellCommand#createGetOpt(org.globus.cog.gridface.impl.gridshell.interfaces.Scope)
	 */
	public GetOpt createGetOpt(Scope scope) {
		GetOpt result = new GetOptImpl(scope);
		result.addArgument(new ArgumentImpl("variable name",String.class,true));
		result.addArgument(new ArgumentImpl("variable value",String.class,true));
		return result;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Command#execute()
	 */
	public Object execute() throws Exception {
		String name = (String)getopt.getArgumentAt(1).getValue();
		String value = (String)getopt.getArgumentAt(2).getValue();
		getParent().getScope().setVariableTo(name,value);
		this.setStatusCompleted();
		return null;
	}



	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent arg0) {
		// do nothing
		
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.interfaces.Command#destroy()
	 */
	public Object destroy() throws Exception {
		// do nothing
		return null;
	}

}
