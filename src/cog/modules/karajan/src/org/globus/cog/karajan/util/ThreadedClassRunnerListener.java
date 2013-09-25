
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 31, 2003
 */
package org.globus.cog.karajan.util;

public interface ThreadedClassRunnerListener {
	void failed(ThreadedClassRunner source, Throwable reason);
	
	void completed(ThreadedClassRunner source);
}
