
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
/*
 * Created on Jun 24, 2003
 */
package org.globus.cog.gui.grapheditor.edges;

import java.awt.Point;

import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.targets.swing.SwingEdgeRenderer;

public class LoopEdgeRenderer extends SwingEdgeRenderer implements ControlPointListener{
	private Seg3 s;

	public LoopEdgeRenderer() {
		s = new Seg3();
		setVisualComponent(s);
	}
	
	public void controlPointUpdated(EdgeComponent source, int index) {
		if (index > 1) {
			Point p = getEdgeComponent().getControlPoint(index);
			s.setPoint(index - 1, p.x, p.y);
		}
	}

	public void setComponent(GraphComponent component) {
		super.setComponent(component);
		EdgeComponent ec = (EdgeComponent) component;
		s.setPoint(1, ec.getControlPoint(2).x, ec.getControlPoint(2).y);
		ec.addControlPointListener(this);
	}

}
