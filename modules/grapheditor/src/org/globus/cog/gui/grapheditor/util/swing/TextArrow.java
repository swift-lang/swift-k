// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 27, 2003
 */
package org.globus.cog.gui.grapheditor.util.swing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;

public class TextArrow extends Arrow {
	protected double alpha, l;
	private String text;

	public void update() {
		ppx = new int[4];
		ppy = new int[4];
		double dx = x2 - x1;
		double dy = y2 - y1;
		l = Math.sqrt(dx * dx + dy * dy);
		alpha = Math.atan2(dy, dx);

		double[] px = new double[4];
		double[] py = new double[4];

		px[0] = l;
		py[0] = 0;
		px[1] = 0;
		py[1] = 0;
		px[2] = l - (double) arrowLength;
		py[2] = (double) arrowWidth / 2.0;
		px[3] = l - (double) arrowLength;
		py[3] = -(double) arrowWidth / 2.0;

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
		if (text != null) {
			Graphics2D g2d = (Graphics2D) g.create();
			LineMetrics lm = g2d.getFontMetrics().getLineMetrics(text, g2d);
			Rectangle2D sb = g2d.getFontMetrics().getStringBounds(text, g2d);
			g2d.translate((x1 + x2) / 2, (y1 + y2) / 2);
			if ((text != null) && (text.length() > 2)) {
				if (alpha > Math.PI / 2) {
					g2d.rotate(alpha + Math.PI);
				}
				else if (alpha < -Math.PI / 2) {
					g2d.rotate(alpha + Math.PI);
				}
				else {
					g2d.rotate(alpha);
				}
			}
			if (sb.getWidth() < l) {
				g2d.drawString(text, (int) (-sb.getCenterX()), -1 - (int) (lm.getAscent() / 2));
			}
			g2d.dispose();
		}
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public TextArrow(int x1, int y1, int x2, int y2, short lwidth, short awidth, short alen,
			String text) {
		super(x1, y1, x2, y2, lwidth, awidth, alen);
		this.text = text;
	}

}