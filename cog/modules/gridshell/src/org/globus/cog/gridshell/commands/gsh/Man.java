/*
 * 
 */
package org.globus.cog.gridshell.commands.gsh;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.commands.AbstractShellCommand;
import org.globus.cog.gridshell.getopt.app.ArgumentImpl;
import org.globus.cog.gridshell.getopt.app.GetOptImpl;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.util.TextFileLoader;

/**
 * 
 */
public class Man extends AbstractShellCommand {
	private static Logger logger = Logger.getLogger(Man.class);
	
	public static final String PROP_MANDIR = "gridshell.command.mandir";
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.AbstractShellCommand#createGetOpt(org.globus.cog.gridshell.interfaces.Scope)
	 */
	public GetOpt createGetOpt(Scope scope) {
		GetOpt result = new GetOptImpl(scope);	
		result.addArgument(new ArgumentImpl("name of command to lookup manpage",String.class,true));
		return result;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.interfaces.Command#execute()
	 */
	public Object execute() throws Exception {
		Gsh shell = this.getGsh();
		if(shell == null) {
			throw new Exception("Parent must be of type Gsh");
		}
		String commandName = String.valueOf(getopt.getArgumentAt(1).getValue());
		AbstractShellCommand command = this.createAbstractShellCommand(commandName);
		
		logger.debug("manPages: "+shell.getManPageMapping().getVariableNames());
		String className = command.getClass().getName();
		String fileName = (String)shell.getManPageMapping().getValue(className);
				
		if(fileName==null) {
		    String parent = shell.getGshEngine().getCommandProperties().getProperty(PROP_MANDIR);
			File f = new File(parent,className+".txt");
			fileName = f.getAbsolutePath();
		}
		
		if(fileName == null) {
			throw new Exception("Error no man page defined for '"+commandName+"'. Check the configuration file.");
		}
		logger.debug("Using file: "+fileName);
		TextFileLoader manLoader = new TextFileLoader();
		try {
			String result = manLoader.loadFromFile(fileName);
			this.setResult(result);
			this.setStatusCompleted();
		}catch(IOException ioException) {
			throw new Exception("Couldn't load manpage ",ioException);
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.interfaces.Command#destroy()
	 */
	public Object destroy() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
