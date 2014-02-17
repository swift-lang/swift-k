//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 19, 2006
 */
package com.sshtools.j2ssh.transport;

import java.io.IOException;

public class ThrottlingException extends IOException {
	public ThrottlingException() {
		super("The server throttled the connection");
	}
}
