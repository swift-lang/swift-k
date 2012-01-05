
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 16, 2004
 */
package org.globus.cog.gui.grapheditor.canvas.views;

import org.globus.cog.gui.grapheditor.edges.EdgeComponent;

public interface Editor {
	
	public void newEdgeRequested(EdgeComponent prototype);
	
	public void cancelEdgeRequest();
	
	public boolean isEditable();
	
	public void setEditable(boolean editable);
}
