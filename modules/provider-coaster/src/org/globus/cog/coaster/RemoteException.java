//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 8, 2006
 */
package org.globus.cog.coaster;


public class RemoteException extends Exception {

	public RemoteException(String msg, Exception remote) {
		super(msg, remote);
	}
}
