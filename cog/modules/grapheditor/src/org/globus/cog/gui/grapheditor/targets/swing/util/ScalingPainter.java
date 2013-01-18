
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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public interface ScalingPainter extends Runnable{
	
	public void setPainterListener(PainterListener pl);
	
	public void setBounds(Rectangle bounds);
	
	public void setPaintArea(Rectangle area);
	
	public BufferedImage getBuffer();
	
	public void setBuffer(BufferedImage buffer);
	
	public void setBufferDimension(Dimension d);
	
	public void cancel();
	
	public void destroy();
	
	public boolean isPainting();
	
	public void wake();
}
