//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 19, 2005
 */
package org.globus.cog.abstraction.impl.slocal;

public interface BinaryOutputListener {
	public void dataReceived(byte[] data, int offset, int length);
}
