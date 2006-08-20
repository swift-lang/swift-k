/*
 * 
 */
package org.globus.cog.gridshell.interfaces;

import java.util.Map;

import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.Status;


/**
 * 
 */
public interface Command extends Runnable, PropertyChangeNotifier, Comparable {
	/**
	 * Used to init this command
	 * @param args - a mapping of variable name to variable value
	 * @return
	 * @throws Exception
	 */	
	Object init(Map args) throws Exception;
	/**
	 * Executes this command
	 * @return
	 * @throws Exception
	 */
	Object execute() throws Exception;
	/**
	 * Suspends this command
	 * @return
	 * @throws Exception
	 */
	Object suspend() throws Exception;	
	/**
	 * Resumes this command
	 * @return
	 * @throws Exception
	 */
	Object resume() throws Exception;
	/**
	 * Kills this command
	 * @return
	 * @throws Exception
	 */
	Object kill() throws Exception;
	/**
	 * Destroys this command - called after the command has been completed
	 * @return
	 * @throws Exception
	 */
	Object destroy() throws Exception;
	/**
	 * Returns a result for this command once the status is COMPLETED
	 * @return
	 */
	Object getResult();
	/**
	 * Returns the scope associated with this command
	 * @return
	 */
	Scope getScope();
	/**
	 * Returns an identity for this command
	 * @return
	 */	
	public Identity getIdentity();
	/**
	 * Returns the parent of this command
	 * @return
	 */
	public Command getParent();
	/**
	 * The status of this command
	 * @return
	 */
	public Status getStatus();
	/**
	 * Sets the status of this Command
	 * @param statusCode
	 */
	public void setStatusCode(int statusCode);
}
