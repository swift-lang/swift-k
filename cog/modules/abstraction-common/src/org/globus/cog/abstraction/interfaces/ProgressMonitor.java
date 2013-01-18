//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 30, 2007
 */
package org.globus.cog.abstraction.interfaces;

public interface ProgressMonitor {
    void progress(long current, long total);
}
