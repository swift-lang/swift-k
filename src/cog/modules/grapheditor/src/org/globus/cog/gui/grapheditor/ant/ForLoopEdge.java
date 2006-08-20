
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;

import org.globus.cog.gui.grapheditor.edges.LoopEdge;

/**
 * A rectangular edge that "represents" a loop in the flow
 */
public class ForLoopEdge extends LoopEdge{
	ForNode peer;
	
	public ForLoopEdge(ForNode peer){
		this.peer = peer;
		peer.setPropertyValue("edgesplit", getControlPoint(2));
	}
}

