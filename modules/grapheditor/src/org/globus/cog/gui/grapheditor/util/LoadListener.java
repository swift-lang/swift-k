
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.util;

import org.globus.cog.gui.grapheditor.GraphComponent;

/**
 * An interface that indicates that the object implementing it is
 * interested in receiveing notification about the load status.
 */
public interface LoadListener {

    public void componentAdded(GraphComponent node);

    public void loadCompleted();
}
