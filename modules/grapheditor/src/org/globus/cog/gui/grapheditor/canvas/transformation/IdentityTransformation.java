
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.canvas.transformation;

import org.globus.cog.util.graph.GraphInterface;

public class IdentityTransformation implements GraphTransformation {
	
	public GraphInterface transform(GraphInterface graph) {
		return graph;
	}

}
