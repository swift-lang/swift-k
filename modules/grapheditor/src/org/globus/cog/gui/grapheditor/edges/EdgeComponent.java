
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.edges;

import org.globus.cog.gui.grapheditor.GraphComponent;

/**
 * An interface for a graph component that can be used to represent an edge
 */
public interface EdgeComponent extends GraphComponent {
    
    public void addControlPointListener(ControlPointListener l);
    
	public void removeControlPointListener(ControlPointListener l);
	
	public ControlPoint updateControlPoint(int cp, int x, int y);
	
	public ControlPoint getControlPoint(int cp);
	
	public int numControlPoints();
}
