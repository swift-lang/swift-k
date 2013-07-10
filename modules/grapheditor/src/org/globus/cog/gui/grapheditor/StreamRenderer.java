
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/* 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor;

import java.io.IOException;
import java.io.Writer;


/**
 * A renderer that can render components on a stream (actually a text stream)
 */
public interface StreamRenderer extends ComponentRenderer{
	public void render(Writer wr) throws IOException;
}
