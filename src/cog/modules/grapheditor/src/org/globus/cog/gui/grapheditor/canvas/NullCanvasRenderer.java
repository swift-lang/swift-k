
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor.canvas;

import java.awt.Dimension;

public class NullCanvasRenderer extends AbstractCanvasRenderer {
	private GraphCanvas canvas;
	
	public void setSize(Dimension dimension) {
	}
	
	public GraphCanvas getCanvas() {
		return canvas;
	}

	public void setCanvas(GraphCanvas canvas) {
		this.canvas = canvas;
	}

}
