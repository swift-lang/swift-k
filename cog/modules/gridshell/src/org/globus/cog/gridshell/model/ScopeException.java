/*
 * 
 */
package org.globus.cog.gridshell.model;

/**
 * 
 */
public class ScopeException extends Exception {
	public ScopeException(String message) {
		super(message);
	}
	public ScopeException(String message,Throwable chainedExceptioin) {
		super(message,chainedExceptioin);
	}

}
