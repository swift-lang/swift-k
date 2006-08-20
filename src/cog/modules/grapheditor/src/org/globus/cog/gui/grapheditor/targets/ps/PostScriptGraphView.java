
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor.targets.ps;

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
import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.EdgeIterator;
import org.globus.cog.util.graph.GraphChangedEvent;
import org.globus.cog.util.graph.Node;
import org.globus.cog.util.graph.NodeIterator;

public class PostScriptGraphView extends AbstractView implements StreamView {
	public static final String LOCATION = "wrapper.location";
	public static final String SIZE = "wrapper.size";

	private static Logger logger = Logger.getLogger(PostScriptGraphView.class);

	private GraphLayoutEngine layoutEngine;

	public PostScriptGraphView() {
		this(null);
	}

	public PostScriptGraphView(GraphLayoutEngine engine) {
		this(engine, "PostScript Graph View");
	}

	public PostScriptGraphView(GraphLayoutEngine engine, String name) {
		setName(name);
		setType("PSGraphView");
		setLayoutEngine(engine);
	}

	public GraphLayoutEngine getLayoutEngine() {
		return layoutEngine;
	}

	public void setLayoutEngine(GraphLayoutEngine layoutEngine) {
		this.layoutEngine = layoutEngine;
	}

	public CanvasView getNewInstance(GraphCanvas canvas) {
		PostScriptGraphView cv = (PostScriptGraphView) super.getNewInstance(canvas);
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


	public void graphChanged(GraphChangedEvent e) {
	}

	/*
	 * This method was taken from JGraph
	 */
	public static Point getRectIntersection(int x, int y, int width, int height, Point p) {
		int xCenter = x + width / 2;
		int yCenter = y + height / 2;
		int dx = p.x - xCenter;
		int dy = p.y - yCenter;
		double alpha = Math.atan2(dy, dx);
		int xout = 0, yout = 0;
		double pi = Math.PI;
		double pi2 = Math.PI / 2.0;
		double beta = pi2 - alpha;
		double t = Math.atan2(height, width);
		if (alpha < -pi + t || alpha > pi - t) { // Left edge
			xout = x;
			yout = yCenter - (int) (width * Math.tan(alpha) / 2);
		}
		else if (alpha < -t) { // Top Edge
			yout = y;
			xout = xCenter - (int) (height * Math.tan(beta) / 2);
		}
		else if (alpha < t) { // Right Edge
			xout = x + width;
			yout = yCenter + (int) (width * Math.tan(alpha) / 2);
		}
		else { // Bottom Edge
			yout = y + height;
			xout = xCenter + (int) (height * Math.tan(beta) / 2);
		}
		return new Point(xout, yout);
	}

	public void clean() {
		super.clean();
	}

	public void render(Writer wr) throws IOException {
		invalidate();
		logger.debug("Graph is "+getGraph());
		if (layoutEngine == null) {
			try {
				Class layoutEngineClass =
					Class.forName(
							(String) getCanvas().getOwner().getRootNode().getPropertyValue(
							"postscript.graphview.layoutengine"));
				layoutEngine = (GraphLayoutEngine) layoutEngineClass.newInstance();
			}
			catch (Exception e) {
				logger.warn(
						"Cannont instantiate requested layout engine ("
						+ getCanvas().getOwner().getRootNode().getPropertyValue(
						"postscript.graphview.layoutengine")
						+ ")",
						e);
				layoutEngine = new HierarchicalLayout();
			}
		}
		logger.debug("Laying out graph using " + getLayoutEngine().getClass().getName());
		Hashtable coords;
		if (getLayoutEngine() instanceof PersistentLayoutEngine2) {
			coords = ((PersistentLayoutEngine2) getLayoutEngine()).layoutGraph(getGraph(), new Hashtable(), true);
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
		maxy += 50;
		wr.write("%%BoundingBox: 0 0 "+(maxx+50-minx)+" "+(maxy+50-miny)+"\n");
		wr.write("%%Creator: Java CoG Kit Graph Editor\n");
		wr.write("%%EndComments\n\n");
		wr.write("/grapheditor save def\n");
		wr.write("/Helvetica findfont 9 scalefont setfont\n");
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
			StreamRenderer renderer = (StreamRenderer) nc.newRenderer("postscript");
			Dimension d;
			if (renderer instanceof GenericPostScriptNodeRenderer) {
				d = ((GenericPostScriptNodeRenderer) renderer).getSize();
			}
			else {
				d = new Dimension(40, 16);
			}
			dimensions.put(n, d);
			wr.write("gsave " + (p.x - minx-d.width/2) + " " + (maxy - p.y -d.height/2) + " translate\n");
			renderer.render(wr);
			wr.write("grestore\n");
		}
		//if somebody could tell me how to deal with font metrics in PS...
		//I suppose they can be pre-calculated
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
			int fx = fp.x - df.width/2;
			int tx = tp.x - dt.width/2;
			int fy = fp.y - df.height/2;
			int ty = tp.y - dt.height/2;
			Point fi = getRectIntersection(fx, fy, df.width, df.height, tp);
			Point ti = getRectIntersection(tx, ty, dt.width, dt.height, fp);
			ec.getControlPoint(0).setLocation(fi.x-minx, maxy-fi.y);
			ec.getControlPoint(1).setLocation(ti.x-minx, maxy-ti.y);
			((StreamRenderer) ec.newRenderer("postscript")).render(wr);
		}
	}
}
