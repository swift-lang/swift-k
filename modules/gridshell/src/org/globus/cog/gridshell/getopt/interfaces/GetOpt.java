/*
 * Created on Dec 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.globus.cog.gridshell.getopt.interfaces;

import java.util.List;
import java.util.Set;

import org.globus.cog.gridshell.interfaces.Scope;

/**
 * GetOpt is what the developer interacts with to parse and get the values arguments and options at the commandline.
 * 
 * 
 */
public interface GetOpt {
	/**
	 * Adds an argument
	 * @param arg
	 */
	void addArgument(Argument arg);
	/**
	 * Adds all arguments
	 * @param args
	 */
	void addArguments(Argument[] args);
	/**
	 * Adds an option
	 * @param option
	 */
	void addOption(Option option);
	/**
	 * Adds all options
	 * @param options
	 */
	void addOptions(Option[] options);	
	
	/**
	 * Determines if a specified option has been added
	 * @param option - the option to check
	 * @return
	 */
	boolean containsOption(Option option);
	/**
	 * Gets a list of all the arguments
	 * @return
	 */
	List getArguments();
	/**
	 * Gets the argument at
	 * @param index
	 * @return
	 */
	Argument getArgumentAt(int index);
	/**
	 * Gets the origional commandline entered
	 * @return
	 */
	String getCommandLineValue();	
	/**
	 * Gets the description of this command
	 * @return
	 */
	String getDescription();	
	/**
	 * Returns the Scope associated with this GetOpt used for Environmental variables
	 * @return
	 */
	Scope getScope();	
	/**
	 * Sets the scope
	 * @param scope
	 */
	void setScope(Scope scope);
	
	/**
	 * Returns a set of all the options
	 * @return
	 */
	Set getOptions();
	
	/**
	 * Gets the option with by the given key
	 * @param key - the key, or name, of the option to get
	 * @return
	 */
	Option getOption(String key);	
	/**
	 * Parse the given commandLineValue
	 * @param commandLineValue - the value entered at commandline
	 */
	void parse(String commandLineValue);
	/**
	 * Checks to see if all required options and args are set, if not throws exception
	 */
	void checkRequired();
		
	/**
	 * Is set to strict args
	 * @return
	 */
	boolean isAllowDynamicArgs();
	/**
	 * If set to true will only allow specified args
	 * else can have more args
	 * @param value
	 */
	void isAllowDynamicArgs(boolean value);
	/**
	 * Can we have dynamic options
	 * @return
	 */
	boolean isAllowDynamicOptions();
	/**
	 * If set to true will allow specified options
	 * else can have more options
	 * @param value
	 */
	void isAllowDynamicOptions(boolean value);
	/**
	 * Doesn't care if the option exists (no exception), sees if it is set
	 * If doesn't exist returns false, if does returns option.isSet()
	 * @param name
	 * @return
	 */
	boolean isOptionSet(String name);
	/**
	 * Doesn't care if the arg exists (no exception), sees if it is set
	 * If doesn't exist returns false, else returns arg.isSet()
	 * @param index
	 * @return
	 */
	boolean isArgSet(int index);
	/**
	 * Sets the description for this command
	 * @param description
	 */
	void setDescription(String description);
}
