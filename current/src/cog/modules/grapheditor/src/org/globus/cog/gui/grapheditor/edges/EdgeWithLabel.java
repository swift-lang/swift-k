
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 27, 2003
 */
package org.globus.cog.gui.grapheditor.edges;

import org.globus.cog.gui.grapheditor.properties.ComponentProperty;

public class EdgeWithLabel extends SimpleArrow {
	private String label;
	
	public EdgeWithLabel() {
		setClassRendererClass(EdgeWithLabelRenderer.class);
		addProperty(new ComponentProperty(this, "label"));
	}
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
