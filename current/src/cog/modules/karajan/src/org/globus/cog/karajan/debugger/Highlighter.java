// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 18, 2005
 */
package org.globus.cog.karajan.debugger;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

public class Highlighter  {
	public static void paint(Graphics2D g2, Color color, int width, int height) {
		g2.setColor(color);
		g2.drawRect(0, 0, width - 1, height - 1);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, (float) 0.3));
		g2.fillRect(1, 1, width - 2, height - 2);
	}
}