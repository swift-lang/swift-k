
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 7, 2003
 */
package org.globus.cog.karajan.util;

/**
 * Elements implementing this interface will receive notifications
 * about load events
 */
public interface LoadListener {
	void loadComplete();
	
	void loadStarted();
}
