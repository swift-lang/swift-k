// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.ssh.execution;

public interface OutputListener {
	public void outputChanged(String s);
	
	public void outputClosed();
}
