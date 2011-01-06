
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


/*
 * Created on Jun 24, 2003
 */
package org.globus.cog.gui.grapheditor.edges;

import java.awt.Color;

import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.targets.swing.SwingEdgeRenderer;
import org.globus.cog.gui.grapheditor.util.swing.ComponentAction;
import org.globus.cog.gui.grapheditor.util.swing.JArrow;

public class SimpleArrowRenderer extends SwingEdgeRenderer{
	ComponentAction properties;
	
	public SimpleArrowRenderer() {
		JArrow arrow = new JArrow();
		setVisualComponent(arrow);
	}

	public void setComponent(GraphComponent component) {
		super.setComponent(component);
		getJArrow().setColor((Color) component.getPropertyValue("color"));
	}

	public JArrow getJArrow() {
		return (JArrow) getVisualComponent();
	}
}
