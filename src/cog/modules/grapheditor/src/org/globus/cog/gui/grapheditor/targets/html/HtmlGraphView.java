
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor.targets.html;

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
import org.globus.cog.gui.grapheditor.targets.swing.views.GraphView;
import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.EdgeIterator;
import org.globus.cog.util.graph.Node;
import org.globus.cog.util.graph.NodeIterator;

public class HtmlGraphView extends AbstractView implements StreamView {
	public static final String LOCATION = "wrapper.location";
	public static final String SIZE = "wrapper.size";

	private static Logger logger = Logger.getLogger(GraphView.class);

	private GraphLayoutEngine layoutEngine;

	public HtmlGraphView() {
		this(null);
	}

	public HtmlGraphView(GraphLayoutEngine engine) {
		this(engine, "HTML Graph View");
	}

	public HtmlGraphView(GraphLayoutEngine engine, String name) {
		setName(name);
		setType("HTMLGraphView");
		setLayoutEngine(engine);
	}

	public GraphLayoutEngine getLayoutEngine() {
		return layoutEngine;
	}

	public void setLayoutEngine(GraphLayoutEngine layoutEngine) {
		this.layoutEngine = layoutEngine;
	}

	public CanvasView getNewInstance(GraphCanvas canvas) {
		HtmlGraphView cv = (HtmlGraphView) super.getNewInstance(canvas);
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
		logger.debug("Graph is "+getGraph());
		logger.debug("Canvas is "+getCanvas());
		logger.debug("Owner is "+getCanvas().getOwner());
		logger.debug("html.graphview.layoutengine is "+getCanvas().getOwner().getRootNode().getPropertyValue("html.graphview.layoutengine"));
		if (layoutEngine == null) {
			try {
				Class layoutEngineClass =
					Class.forName(
						(String) getCanvas().getOwner().getRootNode().getPropertyValue(
							"html.graphview.layoutengine"));
				layoutEngine = (GraphLayoutEngine) layoutEngineClass.newInstance();
			}
			catch (Exception e) {
				logger.warn(
					"Cannont instantiate requested layout engine ("
						+ getCanvas().getOwner().getRootNode().getPropertyValue(
							"html.graphview.layoutengine")
						+ ")",
					e);
				layoutEngine = new HierarchicalLayout();
			}
		}
		logger.debug("Laying out graph using " + getLayoutEngine().getClass().getName());
		Hashtable coords;
		if (getLayoutEngine() instanceof PersistentLayoutEngine2) {
			coords =
				((PersistentLayoutEngine2) getLayoutEngine()).layoutGraph(
					getGraph(),
					new Hashtable(),
					true);
		}
		else {
			coords = getLayoutEngine().layoutGraph(getGraph(), new Hashtable());
		}
		int minx = Integer.MAX_VALUE;
		int miny = Integer.MAX_VALUE;
		int maxx = Integer.MIN_VALUE;
		int maxy = Integer.MIN_VALUE;
		logger.debug("Layout complete");
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
		NodeIterator i = getGraph().getNodesIterator();
		int divid = 0;
		int missed = 0;
		HashMap ids = new HashMap();
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
			ids.put(n, new Integer(divid));
			wr.write(
				"<table id=\"n"
					+ divid++
					+ "\" style=\"position: absolute; top: "
					+ (p.y - miny)
					+ "px; left: "
					+ (p.x - minx)
					+ "px;\"><tr><td>\n");
			//wr.write("<p style=\"background: black;\">");
			 ((StreamRenderer) nc.newRenderer("html")).render(wr);
			wr.write("\n</td></tr></table>\n");
		}
		logger.debug("Nodes rendered");
		//now the problem
		//we don't know from here the sizes of the nodes
		//we'll have to use javascript for that
		//we also have to use images for the edges (until you can draw vector images in browsers)

		StringBuffer froms = new StringBuffer();
		StringBuffer tos = new StringBuffer();
		froms.append("var froms = new Array(");
		tos.append("var tos = new Array(");
		boolean first = true;
		EdgeIterator e = getGraph().getEdgesIterator();
		while (e.hasMoreEdges()) {
			Edge edge = e.nextEdge();
			Node from = edge.getFromNode();
			Node to = edge.getToNode();
			if (!first) {
				froms.append(",");
				tos.append(",");
			}
			else {
				first = false;
			}
			froms.append(((Integer) ids.get(from)).intValue());
			tos.append(((Integer) ids.get(to)).intValue());
		}
		froms.append(");");
		tos.append(");");
		wr.write("<script language=\"JavaScript\">\n");
		wr.write(froms + "\n");
		wr.write(tos + "\n");
		wr.write("var edges=new Array(");
		int count = 0;
		e = getGraph().getEdgesIterator();
		while (e.hasMoreEdges()) {
			if (count % 1000 == 0 && count > 999) {
				logger.info("" + count++ +" edges rendered");
			}
			Edge edge = e.nextEdge();
			EdgeComponent ec = (EdgeComponent) edge.getContents();
			Node from = edge.getFromNode();
			Node to = edge.getToNode();
			Point fp = (Point) coords.get(from);
			Point tp = (Point) coords.get(to);
			ec.getControlPoint(0).setLocation(fp);
			ec.getControlPoint(1).setLocation(tp);
			((StreamRenderer) ec.newRenderer("html")).render(wr);
		}
		wr.write("\"\");\n");
		wr.write("//This one got here all the way from jgraph (jgraph.sourceforge.net)\n");
		wr.write("function getRectIntersection(x, y, w, h, px, py) {\n");
		wr.write("var xc = x + w/2;\n");
		wr.write("var yc = y + h/2;\n");
		wr.write("var dx = px - xc;\n");
		wr.write("var dy = py - yc;\n");
		wr.write("var alpha = Math.atan2(dy, dx);\n");
		wr.write("var xout = 0, yout = 0;\n");
		wr.write("var pi = Math.PI;\n");
		wr.write("var beta = pi / 2 - alpha;\n");
		wr.write("var t = Math.atan2(h, w);\n");
		wr.write("if (alpha < -pi + t || alpha > pi - t) { // Left edge\n");
		wr.write("	xout = x;\n");
		wr.write("	yout = yc - (w * Math.tan(alpha) / 2);\n");
		wr.write("}\n");
		wr.write("else if (alpha < -t) { // Top Edge\n");
		wr.write("	yout = y;\n");
		wr.write("	xout = xc - (h * Math.tan(beta) / 2);\n");
		wr.write("}\n");
		wr.write("else if (alpha < t) { // Right Edge\n");
		wr.write("xout = x + w;\n");
		wr.write("yout = yc + (w * Math.tan(alpha) / 2);\n");
		wr.write("}\n");
		wr.write("else { // Bottom Edge\n");
		wr.write("	yout = y + h;\n");
		wr.write("	xout = xc + (h * Math.tan(beta) / 2);\n");
		wr.write("}\n");
		wr.write("return new Array(xout, yout);\n");
		wr.write("}\n");
		wr.write("function getCrds(id){\n");
		wr.write("var l = document.getElementById(id);\n");
		wr.write("return new Array(l.offsetLeft, l.offsetTop, l.offsetWidth, l.offsetHeight);\n");
		wr.write("}\n");
		wr.write("for (var i = 0; i < froms.length; i++){\n");
		wr.write("var df = getCrds(\"n\"+froms[i]);\n");
		wr.write("var dt = getCrds(\"n\"+tos[i]);\n");
		wr.write("var intf = getRectIntersection(df[0], df[1], df[2], df[3], dt[0], dt[1]);\n");
		wr.write("var intt = getRectIntersection(dt[0], dt[1], dt[2], dt[3], df[0], df[1]);\n");
		wr.write("var x = Math.min(intf[0], intt[0]);\n");
		wr.write("var width = Math.abs(intf[0]-intt[0]);\n");
		wr.write("var y = Math.min(intf[1], intt[1]);\n");
		wr.write("var height = Math.abs(intf[1]-intt[1]);\n");
		//don't zoom vertical and horizontal edges
		wr.write("if (edges[i].indexOf(\"arrow0x\") >= 0){\n");
		wr.write("  width=0;\n");
		wr.write("}\n");
		wr.write("if (edges[i].indexOf(\"x0.png\") >= 0){\n");
		wr.write("  height=0;\n");
		wr.write("}\n");
		wr.write(
			"document.write(\"<div id='e\"+i+\"' style='position: absolute; top: \"+(y-5)+\"px; left: \"+(x-5)+\"px;'>\");\n");
		wr.write(
			"document.write(\"<img src='\"+edges[i]+\"' width='\"+(width+10)+\"' height='\"+(height+10)+\"'>\");\n");
		wr.write("document.write(\"</div>\");\n");
		wr.write("}\n");
		wr.write("</script>\n");
		logger.debug("Edges rendered");
	}
}
