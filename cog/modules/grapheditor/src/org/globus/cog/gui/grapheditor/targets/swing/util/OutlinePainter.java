
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 6, 2004
 *
 */
package org.globus.cog.gui.grapheditor.targets.swing.util;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.targets.swing.views.GraphView;
import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.Node;

public class OutlinePainter extends AbstractPainter {

	private static Logger logger = Logger.getLogger(OutlinePainter.class);

	public static final int SPEED_QUICK_AND_NASTY = 0;
	public static final int SPEED_IN_THE_MIDDLE = 1;
	public static final int SPEED_SLOW_BUT_NICE = 2;

	
	private GraphView view;

	
	private int speed = SPEED_SLOW_BUT_NICE;

	public OutlinePainter(GraphView view) {
		this.view = view;
	}


	public void paintRun() {
		Dimension d = getBufferDimension();
		double sx = d.getWidth() / (bounds.width);
		double sy = d.getHeight() / (bounds.height);
		BufferedImage bi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
		setBuffer(bi);
		Graphics2D g = bi.createGraphics();
		if (speed == SPEED_QUICK_AND_NASTY) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
			g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
				RenderingHints.VALUE_COLOR_RENDER_SPEED);
			g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
			g.setRenderingHint(RenderingHints.KEY_DITHERING,
				RenderingHints.VALUE_DITHER_DISABLE);
			g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_SPEED);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		else if (speed == SPEED_IN_THE_MIDDLE) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
			g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
				RenderingHints.VALUE_COLOR_RENDER_SPEED);
			g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
			g.setRenderingHint(RenderingHints.KEY_DITHERING,
				RenderingHints.VALUE_DITHER_DISABLE);
			g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_SPEED);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		}
		else if (speed == SPEED_SLOW_BUT_NICE) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
				RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g.setRenderingHint(RenderingHints.KEY_DITHERING,
				RenderingHints.VALUE_DITHER_ENABLE);
			g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		}
		g.setBackground((Color) UIManager.get("Label.background"));
		g.clearRect(0, 0, d.width, d.height);
		g.setColor(Color.black);
		AffineTransform at = new AffineTransform();
		at.scale(sx, sy);
		g.setTransform(at);
		paintGraph(g);
	}

	private void paintGraph(Graphics2D g) {
		int count = 0;
		synchronized (view.getGraph()) {
			Edge[] edges = (Edge[]) view.getGraph().getEdgesSet().toArray(new Edge[0]);
			for (int e = 0; e < edges.length; e++) {
				count++;
				if (count % 2000 == 0) {
					if (canceled) {
						return;
					}
					fireBufferUpdated();
				}
				Edge edge = edges[e];
				NodeComponent from = (NodeComponent) edge.getFromNode().getContents();
				NodeComponent to = (NodeComponent) edge.getToNode().getContents();
				Point f = (Point) from.getPropertyValue(GraphView.LOCATION);
				Point t = (Point) to.getPropertyValue(GraphView.LOCATION);
				g.drawLine(f.x - bounds.x, f.y - bounds.y, t.x - bounds.x, t.y - bounds.y);
			}
		}

		Iterator i = view.getGraph().getNodesIterator();
		while (i.hasNext()) {
			count++;
			if (count % 2000 == 0) {
				if (canceled) {
					return;
				}
				fireBufferUpdated();
			}
			Node n = (Node) i.next();
			NodeComponent nc = (NodeComponent) n.getContents();
			Point p = (Point) nc.getPropertyValue(GraphView.LOCATION);
			Dimension d = (Dimension) nc.getPropertyValue(GraphView.SIZE);
			if (d == null) {
				d = GraphView.DEFAULT_SIZE;
			}
			if (nc instanceof ScalableRenderer) {
				((ScalableRenderer) nc).paint(g, p.x - bounds.x - d.width / 2, p.y
					- bounds.y - d.height / 2, d.width, d.height);
			}
			else {
				g.fillRect(p.x - bounds.x - d.width / 2, p.y - bounds.y - d.height / 2,
					d.width, d.height);
			}
		}
	}

	
	public int getSpeed() {
		return this.speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}
}