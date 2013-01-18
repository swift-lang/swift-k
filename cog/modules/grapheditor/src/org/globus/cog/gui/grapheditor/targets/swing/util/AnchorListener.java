
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.targets.swing.util;

/**
 * Objects wanting to receive events from anchors should implement this interface
 * and be added to list of listeners.
 */
public interface AnchorListener{
    public void anchorEvent(AnchorEvent e);
}
