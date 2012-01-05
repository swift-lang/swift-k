
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.ant;

import java.awt.event.ActionListener;

import org.globus.cog.gui.grapheditor.canvas.AbstractCanvas;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.targets.swing.util.GraphComponentWrapperEvent;

/**
 * Contains all the toplevel nodes within a project such as targets.
 */
public class ProjectCanvas extends AbstractCanvas implements GraphCanvas, ActionListener {

	public ProjectCanvas() {
		this(null);
	}

	public ProjectCanvas(ProjectNode owner) {
		super(owner);
		getSupportedNodes().clear();
		getSupportedEdges().clear();
		addNodeType(new TaskNode());
		addNodeType(new EchoNode());
		addNodeType(new TargetNode());
		addNodeType(new ProjectPropertyNode());
		addEdgeType(new TargetDependency());
		addEdgeType(new FlowEdge());
	}

	public void graphComponentEvent(GraphComponentWrapperEvent e) {
	}
}
