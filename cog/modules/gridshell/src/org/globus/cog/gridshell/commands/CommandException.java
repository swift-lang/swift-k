/*
 * 
 */
package org.globus.cog.gridshell.commands;

import org.globus.cog.abstraction.interfaces.Status;

/**
 * A runtime exception when a command fails
 * 
 * 
 */
public class CommandException extends RuntimeException {
	private Status status = null;
	
	public CommandException(String message, Status status) {
		this(message,status,(status != null) ? status.getException() : null);
	}
	
	public CommandException(String message, Status status, Throwable chainedThrowable) {
		super(message,chainedThrowable);
		this.status = status;
	}
}
