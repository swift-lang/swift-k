// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Mar 6, 2004
 *  
 */
package org.globus.cog.gui.grapheditor.targets.swing.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;

public class SwingScalingPainter extends AbstractPainter {

	private static Logger logger = Logger.getLogger(SwingScalingPainter.class);

	private Color background;

	private Component component;
	
	private boolean resized;

	public SwingScalingPainter(Component component, Color background) {
		super();
		this.component = component;
		this.background = background;
	}

	public void paintRun() {
			if ((buffer == null) || resized) {
				buffer = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
				paintArea = null;
				resized = false;
			}
			Graphics2D g2 = buffer.createGraphics();
			g2.setColor(background);
			if (paintArea != null) {
				//g2.setColor(Color.YELLOW);
				g2.fillRect(paintArea.x, paintArea.y, paintArea.width, paintArea.height);
				//g2.setColor(Color.RED);
				g2.drawRect(0, 0, bounds.width-1, bounds.height-1);
				g2.setClip(paintArea);
			}
			else {
				//g2.setColor(Color.GREEN);
				g2.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
			}
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			Graphics2D g3 = (Graphics2D) g2.create();
			g3.translate(-bounds.x, -bounds.y);
			g3.setColor(Color.black);
			g3.setBackground(background);
			paintc(component, g3, false);
	}

	public void paintc(Component c, Graphics g, boolean painted) {
		if (canceled) {
			return;
		}
		if ((c instanceof Container) && !(c instanceof ScalingContainer)
				&& !(c instanceof GraphComponentWrapper)) {
			Container cc = (Container) c;
			Component[] cs = cc.getComponents();
			for (int i = 0; i < cs.length; i++) {
				Point q = cs[i].getLocation();
				Graphics g2 = g.create();
				g2.translate(q.x, q.y);
				paintc(cs[i], g2, painted);
				if (canceled) {
					return;
				}
			}
		}
		if (!painted) {
			try {
				c.paint(g);
			}
			catch (Exception e) {
				logger.debug("Exception caught while painting component " + c, e);
			}
		}
	}
	
	
	public void setBounds(Rectangle bounds) {
		if (bounds != null) {
			if (!bounds.equals(getBounds())) {
				resized = true;
			}
		}
		super.setBounds(bounds);
	}
}