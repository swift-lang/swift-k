// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.grapheditor.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import org.globus.cog.gui.grapheditor.util.RectUtil;
import org.globus.cog.gui.grapheditor.targets.swing.SwingEdge;

/**
 * provides a swing component that can draw an arrow. Additional logic is
 * included since swing/awt components cannot have negative sizes.
 */
public class JArrow extends Component implements SwingEdge {
	private int w, h;
	private Arrow arrow;
	private Color color;

	public JArrow() {
		arrow = new Arrow(0, 0, 32, 32, (short) 1, (short) 6, (short) 6);
		color = Color.BLACK;
	}

	public void setArrow(Arrow a) {
		this.arrow = a;
		updateCoords();
	}

	public Arrow getArrow() {
		return arrow;
	}

	public void setPoint(int index, int x, int y) {
		w = x;
		h = y;
		updateCoords();
	}

	public int getPointX(int index) {
		return w;
	}

	public int getPointY(int index) {
		return h;
	}

	private void updateCoords() {
		arrow.setCoords(0, 0, w, h);
		repaint();
	}

	public void paint(Graphics g) {
		g.setColor(color);
		arrow.paint(g);
	}

	public boolean edgeContains(int x, int y) {
		return arrow.contains(x + 5, y + 5);
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Rectangle getBoundingBox() {
		Rectangle rect = RectUtil.border(new Rectangle(0, 0, w, h), 5);
		return rect;
	}

}