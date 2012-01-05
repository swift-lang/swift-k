// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.grapheditor.edges;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;

import org.globus.cog.gui.grapheditor.targets.swing.SwingEdge;
import org.globus.cog.gui.grapheditor.util.RectUtil;

public class Seg3 extends Component implements SwingEdge {
	int[] x = new int[2];
	int[] y = new int[2];
	int split;
	private Point[] points;

	public Seg3() {
		points = new Point[4];
	}

	public void updateCoords() {
		if (isHorizontal()) {
			//horizontal
			points[0] = new Point(0, 0);
			points[1] = new Point(0, y[1]);
			points[2] = new Point(x[0], y[1]);
			points[3] = new Point(x[0], y[0]);
		}
		else {
			//vertical
			points[0] = new Point(0, 0);
			points[1] = new Point(x[1], 0);
			points[2] = new Point(x[1], y[0]);
			points[3] = new Point(x[0], y[0]);
		}
	}

	private boolean isHorizontal() {
		return Math.abs(x[0]) > Math.abs(y[0]);
	}

	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		for (int i = 0; i < points.length - 1; i++) {
			g.drawLine(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y);
		}
	}

	public boolean contains(int x, int y) {
		for (int i = 0; i < points.length - 1; i++) {
			Line2D.Float line = new Line2D.Float(points[i].x, points[i].y, points[i + 1].x,
					points[i + 1].y);
			if (line.ptSegDist(x, y) < 3) {
				return true;
			}
		}
		return false;
	}

	public void setPoint(int index, int x, int y) {
		this.x[index] = x;
		this.y[index] = y;
		if (index == 1) {
			if (isHorizontal()) {
				split = y;
			}
			else {
				split = x;
			}
		}
		checkSplit();
		updateCoords();
	}

	public boolean edgeContains(int x, int y) {
		return contains(x, y);
	}

	public void checkSplit() {
		if (isHorizontal()) {
			x[1] = x[0] / 2;
			y[1] = split;
		}
		else {
			y[1] = y[0] / 2;
			x[1] = split;
		}
	}

	public int getPointX(int index) {
		return x[index];
	}

	public int getPointY(int index) {
		return y[index];
	}

	public Rectangle getBoundingBox() {
		return RectUtil.grow(new Rectangle(0, 0, x[0], y[0]), x[1], y[1]);
	}
}