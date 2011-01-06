
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 23, 2004
 */
package org.globus.cog.gui.grapheditor.targets.swing;

import org.globus.cog.gui.grapheditor.edges.EdgeComponent;

public class SwingEdgeRenderer extends AbstractSwingRenderer {
	
	public EdgeComponent getEdgeComponent(){
		return (EdgeComponent) getComponent();
	}
	
	public SwingEdge getSwingEdge() {
		return (SwingEdge) getVisualComponent();
	}
	
	public void setPoint(int index, int x, int y) {
		getSwingEdge().setPoint(index, x, y);
	}
}
