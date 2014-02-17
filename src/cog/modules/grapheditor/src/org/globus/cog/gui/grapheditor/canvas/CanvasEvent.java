
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 23, 2004
 */
package org.globus.cog.gui.grapheditor.canvas;


import java.util.EventObject;

import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.Node;

public class CanvasEvent extends EventObject {
	public static final int INVALIDATE = 0;

	public static final int COMPONENT_ADDED = 1;

	public static final int COMPONENT_REMOVED = 2;

	private int type;

	private GraphComponent component;

	private Node node;

	private Edge edge;

	public CanvasEvent(GraphCanvas source, int type) {
		this(source, type, null, null, null);
	}

	public CanvasEvent(GraphCanvas source, int type, GraphComponent component, Node node) {
		this(source, type, component, node, null);
	}
	
	public CanvasEvent(GraphCanvas source, int type, GraphComponent component, Edge edge) {
		this(source, type, component, null, edge);
	}
	
	public CanvasEvent(GraphCanvas source, int type, GraphComponent component, Node node, Edge edge) {
		super(source);
		this.type = type;
		this.component = component;
		this.node = node;
		this.edge = edge;
	}

	public int getType() {
		return type;
	}

	public GraphCanvas getCanvas() {
		return (GraphCanvas) getSource();
	}

	public GraphComponent getComponent() {
		return this.component;
	}

	public Edge getEdge() {
		return this.edge;
	}

	public Node getNode() {
		return this.node;
	}
}
