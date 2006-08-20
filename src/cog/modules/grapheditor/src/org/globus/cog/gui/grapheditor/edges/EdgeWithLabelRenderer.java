
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 27, 2003
 */
package org.globus.cog.gui.grapheditor.edges;

import java.beans.PropertyChangeEvent;

import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.util.swing.JTextArrow;

public class EdgeWithLabelRenderer extends SimpleArrowRenderer {
	public EdgeWithLabelRenderer() {
		JTextArrow arrow = new JTextArrow();
		setVisualComponent(arrow);
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("label")) {
			getJTextArrow().setText((String) evt.getNewValue());
		}
		else {
			super.propertyChange(evt);
		}
	}

	public JTextArrow getJTextArrow() {
		return (JTextArrow) getVisualComponent();
	}
	public void setComponent(GraphComponent component) {
		getJTextArrow().setText((String) component.getPropertyValue("label"));
		super.setComponent(component);
	}

}
