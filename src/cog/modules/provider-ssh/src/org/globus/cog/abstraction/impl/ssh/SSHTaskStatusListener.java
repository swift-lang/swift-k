// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.ssh;

public interface SSHTaskStatusListener {
	public static final int FAILED = 1;
	public static final int COMPLETED = 0;
	public void SSHTaskStatusChanged(int status, Exception e);
}
