
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 22, 2003
 */
package org.globus.cog.gui.grapheditor.targets.swing.util;

import java.awt.Graphics2D;

public interface ScalableRenderer {
	public void paint(Graphics2D g, int x, int y, int w, int h);
}
