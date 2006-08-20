
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 4, 2004
 *
 */
package org.globus.cog.gui.grapheditor.targets.swing.util.export;


import javax.swing.JPanel;

import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.canvas.views.CanvasView;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;

public abstract class ExportPanel extends JPanel {
	private CanvasView view;

	private NodeComponent rootNode;

	public CanvasView getView() {
		return this.view;
	}

	public void setView(CanvasView view) {
		this.view = view;
		rootNode = view.getRenderer().getCanvas().getOwner().getRootNode();
	}
	
	public GraphCanvas getCanvas() {
		return view.getRenderer().getCanvas();
	}

	public abstract void setup();

	public abstract void export();

	public NodeComponent getRootNode() {
		return this.rootNode;
	}
}
