
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.util.swing;

import java.awt.Graphics;
import java.awt.geom.Line2D;

/**
 * Draws an arrow composed of three lines
 */
public class Arrow {

	/** Stores the bounding polygon for the edge with an arrow. */
	protected int x1, x2, y1, y2;
	protected short lineWidth, arrowWidth, arrowLength;
	protected boolean valid;
	protected int[] ppx, ppy;

	public Arrow() {
		this(0, 0, 5, 5, (short) 1, (short) 1, (short) 1);
	}

	public Arrow(
		int x1,
		int y1,
		int x2,
		int y2,
		short lwidth,
		short awidth,
		short alen) {
		valid = false;
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		this.lineWidth = lwidth;
		this.arrowWidth = awidth;
		this.arrowLength = alen;
	}

	public void setCoords(int x1, int y1, int x2, int y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		valid = false;
	}

	public void setLineWidth(short lwidth) {
		this.lineWidth = lwidth;
		valid = false;
	}

	public void setArrowSize(short awidth, short alen) {
		this.arrowWidth = awidth;
		this.arrowLength = alen;
		valid = false;
	}

	public void update() {
		ppx = new int[4];
		ppy = new int[4];
		double dx = x2 - x1;
		double dy = y2 - y1;
		double l = Math.sqrt(dx * dx + dy * dy);
		double alpha = Math.atan2(dy , dx);
		
		double[] px = new double[4];
		double[] py = new double[4];

		px[0] = l;
		py[0] = 0;
		px[1] = 0;
		py[1] = 0;
		px[2] = l - arrowLength;
		py[2] = arrowWidth / 2.0;
		px[3] = l - arrowLength;
		py[3] = - (double) arrowWidth / 2.0;

		for (int i = 0; i < 4; i++) {
			double rx, ry;

			rx = px[i] * Math.cos(alpha) - py[i] * Math.sin(-alpha);
			ry = -py[i] * Math.cos(-alpha) + px[i] * Math.sin(alpha);

			ppx[i] = x1 + (int) Math.round(rx);
			ppy[i] = y1 + (int) Math.round(ry);
		}
		valid = true;
	}

	public void paint(Graphics g) {
		if (!valid) {
			update();
		}
		g.drawLine(ppx[0], ppy[0], ppx[1], ppy[1]);
		g.drawLine(ppx[0], ppy[0], ppx[2], ppy[2]);
		g.drawLine(ppx[0], ppy[0], ppx[3], ppy[3]);
	}

	/**
	 * This method checks whether the given point with coordinates x and y
	 * lies on the edge or outside the edge.
	 * @param x - x co-ordinate of the point.
	 * @param y - y co-ordinate of the point.
	 */
	public boolean contains(int x, int y) {
		if (!valid) {
			update();
		}
		for (int i = 1; i < 4; i++) {
			Line2D.Float line = new Line2D.Float(ppx[0], ppy[0], ppx[i], ppy[i]);
			if (line.ptSegDist(x, y) < 3) {
				return true;
			}
		}
		return false;
	}
}
