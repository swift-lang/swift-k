
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
/*
 * Created on Jun 24, 2003
 */
package org.globus.cog.gui.grapheditor.edges;

import org.globus.cog.gui.grapheditor.AbstractRenderer;


public class EdgeRenderer extends AbstractRenderer{
	public EdgeComponent getEdge(){
		return (EdgeComponent) getComponent();
	}

}
