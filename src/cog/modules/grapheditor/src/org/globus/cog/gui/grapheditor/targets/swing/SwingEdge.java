
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Feb 7, 2004
 *
 */
package org.globus.cog.gui.grapheditor.targets.swing;

import java.awt.Rectangle;


public interface SwingEdge {
	
	public void setPoint(int index, int width, int height);
	
	public int getPointX(int index);
	
	public int getPointY(int index);
	
	public boolean edgeContains(int x, int y);
	
	/**
	 * Relative to the start point
	 * This is needed because some twisted edges may have 
	 * unusual forms that do not fall in the rectangle determined by the
	 * start end end points
	 */
	public Rectangle getBoundingBox();
}
