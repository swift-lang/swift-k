
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


/*
 * Created on Aug 26, 2003
 */
package org.globus.cog.gui.grapheditor.targets.swing.util;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.targets.swing.views.GraphView;
import org.globus.cog.gui.grapheditor.util.swing.RepaintMonitoringContainer;
import org.globus.cog.util.graph.Node;

public class OverviewContainer extends ScalingContainer {
	private Logger logger = Logger.getLogger(OverviewContainer.class);

	private Rectangle visible;

	private GraphView view;

	private boolean useScaling;
	
	private static final int OUTLINE_PAINTER = 10;

	public OverviewContainer(GraphView view) {
		super(view.getComponent());
		this.view = view;
		visible = new Rectangle(0, 0, 0, 0);
	}

	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Color.RED);
		Point p1 = scaledToReal(visible.getLocation());
		Point p2 = scaledToReal((int) (visible.getX() + visible.getWidth()),
			(int) (visible.getY() + visible.getHeight()));
		g.drawRect(p1.x, p1.y, p2.x - p1.x - 1, p2.y - p1.y - 1);
	}

	public Dimension computeSize() {
		if (view.isSelective()) {
			calculateGraphSize();
			return new Dimension(getMaxx() - getMinx(), getMaxy() - getMiny());
		}
		else {
			return super.computeSize();
		}
	}

	private void calculateGraphSize() {
		Iterator i = view.getGraph().getNodesIterator();
		int minx = 0;
		int maxx = 0;
		int miny = 0;
		int maxy = 0;
		while (i.hasNext()) {
			Node n = (Node) i.next();
			NodeComponent nc = (NodeComponent) n.getContents();
			Point p = (Point) nc.getPropertyValue(GraphView.LOCATION);
			Dimension d = (Dimension) nc.getPropertyValue(GraphView.SIZE);
			if (d == null) {
				d = GraphView.DEFAULT_SIZE;
			}
			if (p.x < minx) {
				minx = p.x;
			}
			if (p.x > maxx) {
				maxx = p.x + d.width;
			}
			if (p.y < miny) {
				miny = p.y;
			}
			if (p.y > maxy) {
				maxy = p.y + d.height;
			}
		}
		setMinx(minx);
		setMiny(miny);
		setMaxx(maxx);
		setMaxy(maxy);
	}

	public void paintOutline() {
		calculateGraphSize();
		ScalingPainter painter = getPainter(OUTLINE_PAINTER);
		painter.setBounds(new Rectangle(getMinx(), getMiny(), getMaxx()
			- getMinx(), getMaxy() - getMiny()));
		painter.setBufferDimension(getBufferSize());
		painter.setPainterListener(this);
		painter.wake();
	}

	protected ScalingPainter getPainter(int type) {
		if (type == OUTLINE_PAINTER) {
			if (!(painter instanceof OutlinePainter)) {
				destroyCurrentPainter();
				painter = new OutlinePainter(view);
				painter.setPainterListener(this);
				new Thread(painter).start();
			}
			return painter;
		}
		return super.getPainter(type);
	}
	
	public void setVisibleArea(Rectangle r) {
		this.visible = r;
	}

	public boolean getUseScaling() {
		return useScaling;
	}

	public void setUseScaling(boolean b) {
		this.useScaling = b;
	}

	public void paintBuffer() {
		if (view.isSelective()) {
			setOutline(true);
		}
		else {
			setOutline(false);
		}
		super.paintBuffer();
	}

	public void setComponent(Component comp) {
		if (getComponent() instanceof RepaintMonitoringContainer) {
			((RepaintMonitoringContainer) getComponent())
				.removeRepaintListener(this);
		}
		this.comp = comp;
		if (getComponent() instanceof RepaintMonitoringContainer) {
			((RepaintMonitoringContainer) getComponent())
				.addRepaintListener(this);
		}
		if (comp != null) {
			comp.setVisible(true);
			comp.setLocation(0, 0);
			comp.invalidate();
			comp.validate();
			invalidate();
		}
	}

	public void dispose() {
		super.dispose();
		setComponent(null);
	}
}
