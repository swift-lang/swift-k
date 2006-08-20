/*
 * Created on Dec 25, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.globus.cog.gridshell.interfaces;

/**
 * 
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface StdOutputListener {
	/**
	 * Outputs message to standard output
	 * @param message
	 */
	void stdOutput(Object message);
	/**
	 * Outputs message to standard error
	 * @param message
	 */
	void stdErrOutput(Object message);
	/**
	 * Outputs the exception to standard error Allows for stack traces to be gotten
	 * @param e
	 */
	void stdErrOutput(Exception e);
}
