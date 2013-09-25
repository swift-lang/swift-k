
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor.targets.svg;


import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.StreamRenderer;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.canvas.views.AbstractView;
import org.globus.cog.gui.grapheditor.canvas.views.CanvasView;
import org.globus.cog.gui.grapheditor.canvas.views.StreamView;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.GraphLayoutEngine;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.HierarchicalLayout;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.PersistentLayoutEngine2;
import org.globus.cog.gui.grapheditor.edges.EdgeComponent;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.targets.ps.PostScriptGraphView;
import org.globus.cog.gui.grapheditor.targets.swing.views.GraphView;
import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.EdgeIterator;
import org.globus.cog.util.graph.Node;
import org.globus.cog.util.graph.NodeIterator;

public class SVGGraphView extends AbstractView implements StreamView {
	private static Logger logger = Logger.getLogger(SVGGraphView.class);

	private GraphLayoutEngine layoutEngine;

	public SVGGraphView() {
		this(null);
	}

	public SVGGraphView(GraphLayoutEngine engine) {
		this(engine, "SVG Graph View");
	}

	public SVGGraphView(GraphLayoutEngine engine, String name) {
		setName(name);
		setType("SVGGraphView");
		setLayoutEngine(engine);
	}

	public GraphLayoutEngine getLayoutEngine() {
		return layoutEngine;
	}

	public void setLayoutEngine(GraphLayoutEngine layoutEngine) {
		this.layoutEngine = layoutEngine;
	}

	public CanvasView getNewInstance(GraphCanvas canvas) {
		SVGGraphView cv = (SVGGraphView) super.getNewInstance(canvas);
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
		logger.debug("Graph is " + getGraph());
		if (layoutEngine == null) {
			try {
				Class layoutEngineClass = Class.forName((String) getCanvas().getOwner()
					.getRootNode().getPropertyValue("svg.graphview.layoutengine"));
				layoutEngine = (GraphLayoutEngine) layoutEngineClass.newInstance();
			}
			catch (Exception e) {
				logger.warn("Cannont instantiate requested layout engine ("
					+ getCanvas().getOwner().getRootNode().getPropertyValue(
						"svg.graphview.layoutengine") + ")", e);
				layoutEngine = new HierarchicalLayout();
			}
		}
		logger.debug("Laying out graph using " + getLayoutEngine().getClass().getName());
		Hashtable coords;
		if (getLayoutEngine() instanceof PersistentLayoutEngine2) {
			coords = ((PersistentLayoutEngine2) getLayoutEngine()).layoutGraph(getGraph(),
				new Hashtable(), true);
		}
		else {
			coords = getLayoutEngine().layoutGraph(getGraph(), new Hashtable());
		}
		int minx = Integer.MAX_VALUE;
		int miny = Integer.MAX_VALUE;
		int maxx = Integer.MIN_VALUE;
		int maxy = Integer.MIN_VALUE;
		Iterator c = coords.values().iterator();
		while (c.hasNext()) {
			Point p = (Point) c.next();
			if (minx > p.x) {
				minx = p.x;
			}
			if (miny > p.y) {
				miny = p.y;
			}
			if (maxx < p.x) {
				maxx = p.x;
			}
			if (maxy < p.y) {
				maxy = p.y;
			}
		}
		minx -= 50;
		miny -= 50;
		wr
			.write("<svg width=\""
				+ (maxx + 50 - minx)
				+ "\" height=\""
				+ (maxy + 50 - miny)
				+ "\" viewBox=\"0 0 "
				+ (maxx + 50 - minx)
				+ " "
				+ (maxy + 50 - miny)
				+ "\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version="
				+ "\"1.1\">\n");
		wr.write("<desc>Java CoG Kit Graph Editor</desc>\n");
		NodeIterator i = getGraph().getNodesIterator();
		int divid = 0;
		int missed = 0;
		HashMap dimensions = new HashMap();

		while (i.hasMoreNodes()) {
			if (divid % 1000 == 0 && divid > 999) {
				logger.info("" + divid + " nodes rendered");
			}
			Node n = i.nextNode();
			NodeComponent nc = (NodeComponent) n.getContents();
			Point p = (Point) coords.get(n);
			if (p == null) {
				p = new Point(50 * missed++, 10);
				coords.put(n, p);
			}
			StreamRenderer renderer = (StreamRenderer) nc.newRenderer("svg");
			Dimension d;
			if (nc.hasProperty(GraphView.SIZE)) {
				d = (Dimension) nc.getPropertyValue(GraphView.SIZE);
			}
			else {
				d = new Dimension(40, 24);
			}
			dimensions.put(n, d);
			wr.write("<g transform=\"translate(" + (p.x - d.width / 2 - minx) + ", "
				+ (p.y - d.height / 2 - miny) + ")\">\n");
			renderer.render(wr);
			wr.write("</g>\n");
		}
		wr.write("<g stroke=\"black\">\n");
		EdgeIterator e = getGraph().getEdgesIterator();
		while (e.hasMoreEdges()) {
			Edge edge = e.nextEdge();
			EdgeComponent ec = (EdgeComponent) edge.getContents();
			Node from = edge.getFromNode();
			Node to = edge.getToNode();
			Dimension df = (Dimension) dimensions.get(from);
			Dimension dt = (Dimension) dimensions.get(to);
			Point fp = (Point) coords.get(from);
			Point tp = (Point) coords.get(to);
			logger.debug("F: " + fp + ", T: " + tp);
			int fx = fp.x - df.width / 2;
			int tx = tp.x - dt.width / 2;
			int fy = fp.y - df.height / 2;
			int ty = tp.y - dt.height / 2;
			Point fi = PostScriptGraphView.getRectIntersection(fx, fy, df.width, df.height, tp);
			Point ti = PostScriptGraphView.getRectIntersection(tx, ty, dt.width, dt.height, fp);
			logger.debug("FI: " + fi + ", TI: " + ti);
			ec.getControlPoint(0).setLocation(fi.x - minx, fi.y - miny);
			ec.getControlPoint(1).setLocation(ti.x - minx, ti.y - miny);
			((StreamRenderer) ec.newRenderer("svg")).render(wr);
		}
		wr.write("</g>\n");
		wr.write("</svg>\n");
	}
}
