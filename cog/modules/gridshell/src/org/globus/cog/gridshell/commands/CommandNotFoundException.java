/*
 * 
 */
package org.globus.cog.gridshell.commands;

/**
 * 
 */
public class CommandNotFoundException extends Exception {
	public CommandNotFoundException(String message) {
		super(message);
	}
	
	public CommandNotFoundException(String message, Throwable chained) {
		super(message,chained);
	}
}
