
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor.targets.dot;


import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.StreamRenderer;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.canvas.views.AbstractView;
import org.globus.cog.gui.grapheditor.canvas.views.CanvasView;
import org.globus.cog.gui.grapheditor.canvas.views.StreamView;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.GraphLayoutEngine;
import org.globus.cog.gui.grapheditor.edges.EdgeComponent;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.EdgeIterator;
import org.globus.cog.util.graph.Node;
import org.globus.cog.util.graph.NodeIterator;

public class DotGraphView extends AbstractView implements StreamView {
	private static Logger logger = Logger.getLogger(DotGraphView.class);

	private GraphLayoutEngine layoutEngine;

	public DotGraphView() {
		this(null);
	}

	public DotGraphView(GraphLayoutEngine engine) {
		this(engine, "Dot Graph View");
	}

	public DotGraphView(GraphLayoutEngine engine, String name) {
		setName(name);
		setType("DotGraphView");
		setLayoutEngine(engine);
	}

	public GraphLayoutEngine getLayoutEngine() {
		return layoutEngine;
	}

	public void setLayoutEngine(GraphLayoutEngine layoutEngine) {
		this.layoutEngine = layoutEngine;
	}

	public CanvasView getNewInstance(GraphCanvas canvas) {
		DotGraphView cv = (DotGraphView) super.getNewInstance(canvas);
		cv.setLayoutEngine(getLayoutEngine());
		cv.setName(getName());
		return cv;
	}

	public void invalidate() {
		invalidate(false);
	}

	public void reLayout() {
		invalidate(true);
	}

	public void invalidate(boolean ignoreOld) {
		super.invalidate();

	}

	public void clean() {
		super.clean();
	}

	public void render(Writer wr) throws IOException {
		invalidate();
		NodeIterator i = getGraph().getNodesIterator();
		while (i.hasMoreNodes()) {
			Node n = i.nextNode();
			NodeComponent nc = (NodeComponent) n.getContents();
			StreamRenderer renderer = (StreamRenderer) nc.newRenderer("dot");
			renderer.render(wr);
		}
		EdgeIterator e = getGraph().getEdgesIterator();
		while (e.hasMoreEdges()) {
			Edge edge = e.nextEdge();
			EdgeComponent ec = (EdgeComponent) edge.getContents();
			Node from = edge.getFromNode();
			Node to = edge.getToNode();
			NodeComponent nf = (NodeComponent) from.getContents();
			NodeComponent nt = (NodeComponent) to.getContents();
			ec.setPropertyValue("from", nf.getPropertyValue("nodeid"));
			ec.setPropertyValue("to", nt.getPropertyValue("nodeid"));
			((StreamRenderer) ec.newRenderer("dot")).render(wr);
		}
	}
}
