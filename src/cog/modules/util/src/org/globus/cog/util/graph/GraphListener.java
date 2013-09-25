
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.util.graph;

/**
 * An interface for objects wanting to listen to structural changes in graphs
 */
public interface GraphListener {

    public void graphChanged(GraphChangedEvent e);
}