
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
/*
 * Created on Jun 24, 2003
 */
package org.globus.cog.gui.grapheditor;



public class NullRenderer implements ComponentRenderer{
	public NullRenderer(){
	}

	public void setComponent(GraphComponent comp) {	
	}

	public GraphComponent getComponent() {
		return null;
	}

	public Object getVisualComponent() {
		return null;
	}

	public void dispose() {		
	}
}
