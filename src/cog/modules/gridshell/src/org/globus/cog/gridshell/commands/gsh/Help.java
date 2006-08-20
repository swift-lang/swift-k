/*
 * 
 */
package org.globus.cog.gridshell.commands.gsh;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.commands.AbstractShellCommand;
import org.globus.cog.gridshell.getopt.app.ArgumentImpl;
import org.globus.cog.gridshell.getopt.app.GetOptImpl;
import org.globus.cog.gridshell.getopt.interfaces.Argument;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.getopt.interfaces.Option;
import org.globus.cog.gridshell.interfaces.Scope;

/**
 * 
 */
public class Help extends AbstractShellCommand {
	private static Logger logger = Logger.getLogger(Help.class);
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.commands.AbstractShellCommand#createGetOpt(org.globus.cog.gridshell.interfaces.Scope)
	 */
	public GetOpt createGetOpt(Scope scope) {
		GetOpt result = new GetOptImpl(scope);
		result.addArgument(new ArgumentImpl("command name to display help for",String.class,false));
		return result;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.interfaces.Command#execute()
	 */
	public Object execute() throws Exception {
		// if for specific command
		Object result = null;
		if(getopt.getArgumentAt(1).isSet()) {
			result = singleCommand();
		}else {
			// if it is for listing of commands
			result = allCommands();
		}
		setResult(result);
		setStatusCompleted();
		
		return null;
	}
	
	public Object singleCommand() throws Exception {
		String commandName = String.valueOf(getopt.getArgumentAt(1).getValue());
		AbstractShellCommand ashCommand = this.createAbstractShellCommand(commandName);

		// don't need the scope
		GetOpt getopt = Gsh.createGetOpt(ashCommand,null);

		StringBuffer result = new StringBuffer();
		result.append("Help for ");
		result.append(commandName);
		result.append("\n");
		result.append(getOptToString(getopt));
		return result;
	}
	
	public static String getOptToString(GetOpt getopt) {
		logger.debug("getOptToString ( "+getopt+" )");
		if(getopt == null) {
			return "null";
		}		
		StringBuffer result = new StringBuffer();
		Collection options = getopt.getOptions();
		Collection args = getopt.getArguments();
		// used as a temp value
		String value = null;
		
		// first add options
		logger.debug("option.size="+options.size());
		Iterator iOptions = options.iterator();
		while(iOptions.hasNext()) {
			Option option = (Option)iOptions.next();
			logger.debug("option="+option);
			boolean isRequired = option.isRequired();
			
			// short long options
			if(!isRequired) {
				result.append("[");
			}			
			value = option.getShort();
			if(value != null) {
				result.append(value);
			}else {
				result.append("  ");
			}
			result.append(",");
			value = option.getLong();
			if(value != null) {
				result.append(value);
			}
			if(!option.isFlag()) {
				// the type
				result.append(" <");
				result.append(option.getType());
				result.append(">");
			}
			if(!isRequired) {
				result.append("]");
			}
			
			// description
			result.append("\t");
			result.append(option.getDescription());			
			result.append("\n");			
		}
		// then add args
		logger.debug("args.size="+getopt.getArguments().size());
		Iterator iArgs = getopt.getArguments().iterator();
		while(iArgs.hasNext()) {
			Argument argument = (Argument)iArgs.next();
			logger.debug("argument="+argument);
			boolean isRequired = argument.isRequired();
			
			if(!isRequired) {
				result.append("[");
			}			
			result.append("<");
			result.append(argument.getType());
			result.append(">");
			if(!isRequired) {
				result.append("]");
			}
			
			result.append("\t");
			result.append(argument.getDescription());			
			result.append("\n");			
		}	
 
		
		return result.toString();
	}
	
	public Object allCommands() throws Exception {
		StringBuffer result = new StringBuffer();
		// this command is specifically for Gsh
		Gsh shell = getGsh();
		if(shell==null) {
			throw new Exception("Shell must be of type Gsh");
		}else {
			Iterator iCommandNames = shell.getAvaialbeCommandNames().iterator();
			while(iCommandNames.hasNext()) {
				String s = String.valueOf(iCommandNames.next());
				result.append(s);
				result.append("\n");
			}
		}
		return result;		
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent arg0) {
		// do not need to receive property change events
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.interfaces.Command#destroy()
	 */
	public Object destroy() throws Exception {
		// do nothing method
		return null;
	}
	
}
