/*
 * 
 */
package org.globus.cog.gridshell.commands.gsh;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.GridShellProperties;
import org.globus.cog.gridshell.commands.AbstractCommand;
import org.globus.cog.gridshell.commands.AbstractShellCommand;
import org.globus.cog.gridshell.commands.CommandNotFoundException;
import org.globus.cog.gridshell.commands.CommandProperties;
import org.globus.cog.gridshell.getopt.app.ArgumentImpl;
import org.globus.cog.gridshell.getopt.app.GetOptImpl;
import org.globus.cog.gridshell.getopt.app.OptionImpl;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.interfaces.Command;
import org.globus.cog.gridshell.interfaces.GridShell;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.model.ScopeableProperties;

/**
 * 
 */
public class GshEngine {
    private static final Logger logger = Logger.getLogger(GshEngine.class);
    
    private GridShell gsh;
    private Gsh program;
    
    private CommandProperties commandProp;
    
    public Scope getScope() {
        return program.getScope();
    }
	     
    public void init(Object gsh) throws Exception {
        this.gsh = (GridShell)gsh;
        this.program = (Gsh)gsh;
        
		// the commandproperties
		if(commandProp == null) {
			String propName = "gridshell.program.location."+gsh.getClass().getName();
			String fileName = GridShellProperties.getDefault().getProperty(propName);

			if(fileName != null) {
			  logger.info("command properties not null");
			  File file = new File(fileName);			  
			  try {
			      commandProp = new CommandProperties(file);
			  }catch(Exception exception) {
			  	// give a useful message
			  	String message = "Can't load Gsh commands. Check file associated with property '"+propName+"' of value '"+file+"' in GridShellProperties file @ "+GridShellProperties.getDefault().getFile()+".";
			  	throw new Exception(message,exception);
			  }
			}else {
				String message = "Can't load Gsh commands. Reason: couldn't find property '"+propName+"' in GridShellProperties file @ "+GridShellProperties.getDefault().getFile()+". Check your property file configuraiton.";
				throw new Exception(message);
			}
		}
    }
    
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Program#createCommand(java.util.Map)
	 */
	public Command createCommand(Map args) throws Exception {
		logger.info("createCommand");
		AbstractShellCommand result = null;
		
		String commandEntered = (String)args.get(Gsh.PARAM_createCommand_commandValue);		
		
		// parse to get the commandName
		String commandName = getCommandName(commandEntered);
		
		// given the name create the command object
		result = createCommand(commandName);
		// initialize the command
		initCommand(result,commandEntered);
				
		// return the result
		return result;
	}
	
	/**
	 * Just a helper method to break things down
	 * @param commandEntered
	 * @return
	 */
	protected String getCommandName(String commandEntered) {
		String result = null;
		GetOpt gshGetopt = new GetOptImpl(getScope());
		gshGetopt.isAllowDynamicArgs(true);
		gshGetopt.isAllowDynamicOptions(true);
		gshGetopt.parse(commandEntered);
				
		// get the command name
		result = (String) gshGetopt.getArgumentAt(0).getValue();
		
		// get the last argument, is it &, if so then run in background
		int last = gshGetopt.getArguments().size() - 1;
		Object lastArg = gshGetopt.getArgumentAt(last).getValue();
		if("&".equals(lastArg)) {
		    gsh.acceptCommandState();
		}
		return result;
	}
	
	/**
	 * A helper method so that errors are very carefully described. Only creats the command, still needs initialized.
	 * @param commandName
	 * @param commandEntered
	 * @return
	 * @throws Exception
	 */
	public AbstractShellCommand createCommand(String commandName) throws Exception {
		AbstractShellCommand result = null;
		
		// get the class name
		gsh.getCommands();
		String className = (String)commandProp.getCommandValue(commandName);
		logger.debug("commandName="+commandName+" className="+className);
		if(commandName==null) {
			throw new IllegalArgumentException("The value of commandName cannot be null.");
		}else if(className == null){
			throw new CommandNotFoundException("Class name was not defined for "+commandName+" please ensure that it is defined in "+this.commandProp.getFile()+" or an inherited file.");
		}
		
		try {
			// create result object
			Class resultClass = Class.forName(className);
			result = (AbstractShellCommand)resultClass.newInstance();			
		}catch(ClassNotFoundException exception) {
			// forName
			throw new Exception("Could not find the class '" + className
					+ "' for '" + commandName + "' as defined in "
					+ this.commandProp.getFile(), exception);			
		}catch(ClassCastException exception){
			// newInstance
			throw new Exception("The class '" + className
					+ "' must be of type AbstactShellCommand as defined for '" + commandName + "' in "
					+ this.commandProp.getFile(), exception);
		}catch(IllegalAccessException exception) {
			// newInstance
			throw new Exception("Could not initialize class '" + className
					+ "' for '" + commandName + "' as defined in "
					+ this.commandProp.getFile(), exception);
		}catch(InstantiationException exception) {
			// newInstance
			throw new Exception("Could not initialize class '" + className
					+ "' for '" + commandName + "' as defined in "
					+ this.commandProp.getFile(), exception);
		}
		
		return result;
	}
	
	public void initCommand(AbstractShellCommand command, String commandEntered) throws Exception {
		try {
			//	 get the getopt for the command
			GetOpt commandGetopt = createGetOpt(command,getScope());
			
			// parse for this command
			commandGetopt.parse(commandEntered);
			// initialize result
			command.init(AbstractCommand.arrayToMap(new Object[] {"parent",gsh,"getopt",commandGetopt}));
		}catch(RuntimeException exception) {
			// parse failed
			throw new Exception("Failed to parse the value "+commandEntered+" for command '" + command.getClass().getName()
					+ "' as defined in " + this.commandProp.getFile(), exception);
		}catch(Exception exception) {
			// init failed
			throw new Exception("Failed to initialze the command ",exception);
		}
	}
	
	/**
	 * Creates a getopt for given command, adds the help option, and adds the background argument
	 * @param command
	 * @param scope
	 * @return
	 */
	public static GetOpt createGetOpt(AbstractShellCommand command, Scope scope) {
		// get the getopt for the command
		GetOpt result = command.createGetOpt(scope);
		// we always allow the flag "help"
		result.addOption(OptionImpl.createFlag("displays help",null,"help"));
		result.addArgument(new ArgumentImpl("& to run in background",String.class,false));
		
		return result;	
	}
	
	public ScopeableProperties getProperties() {
	    return commandProp;
	}
	
	public Collection getAvaialbeCommandNames() {
		return commandProp.getSubScope(CommandProperties.PREFIX_COMMAND).getVariableNames();
	}
	public Scope getManPageMapping() {
		return commandProp.getManPages();
	}
	
	public CommandProperties getCommandProperties() {
	    return commandProp;
	}
}
