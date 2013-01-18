
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 6, 2004
 *
 */
package org.globus.cog.gui.grapheditor.targets.swing.util;

public interface PainterListener {
	public void paintCompleted(ScalingPainter source);
	
	public void bufferUpdated(ScalingPainter source);
}
