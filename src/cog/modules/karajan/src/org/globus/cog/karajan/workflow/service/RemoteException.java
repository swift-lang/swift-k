//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 8, 2006
 */
package org.globus.cog.karajan.workflow.service;

import java.io.PrintStream;
import java.io.PrintWriter;

public class RemoteException extends Exception {
	private String remote;
	public RemoteException(String msg, String remote) {
		super(msg);
		this.remote = remote;
	}
	
	public void printStackTrace(PrintStream s) {
		super.printStackTrace(s);
		s.println("Remote exception:");
		s.println(remote);
	}
	
	public void printStackTrace(PrintWriter s) {
		super.printStackTrace(s);
		s.println("Remote exception:");
		s.println(remote);
	}
}
