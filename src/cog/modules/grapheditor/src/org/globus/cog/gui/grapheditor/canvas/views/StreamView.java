
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor.canvas.views;

import java.io.IOException;
import java.io.Writer;


public interface StreamView extends CanvasView{
	public void render(Writer wr) throws IOException;
}
