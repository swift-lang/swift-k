/*
 * 
 */
package org.globus.cog.gridshell.interfaces;

import java.util.Collection;
import java.util.Map;

/**
 * 
 */
public interface Program extends Command {
	/**
	 * Returns a collection of the commands submitted
	 * @return
	 */
	public Collection getCommands();
	/**
	 * Adds a command to the commands submitted to this program
	 * @param command
	 */
	public void addCommand(Command command);
	/**
	 * Removes a command from the commands submitted to this program
	 * @param command
	 */
	public void removeCommand(Command command);
	/**
	 * Creates a command
	 * @param args
	 * @throws Exception
	 */
	public Command createCommand(Map args) throws Exception;
	/**
	 * To run a command (ie calls command.execute change states while it runs)
	 * @param command
	 */
	public void executeCommand(Command command);
	/**
	 * Calls createCommand(Map args).execute()
	 * @param command
	 * @throws Exception
	 */
	public void createAndExecuteCommand(Map args);
}
