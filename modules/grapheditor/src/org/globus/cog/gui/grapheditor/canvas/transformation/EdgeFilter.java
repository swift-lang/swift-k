
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.canvas.transformation;

import java.util.Iterator;

import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.Graph;
import org.globus.cog.util.graph.GraphInterface;

public class EdgeFilter implements GraphTransformation {
	private Class edgeFilter;
	
	public EdgeFilter(Class edgeFilter){
		this.edgeFilter = edgeFilter;
	}

	public GraphInterface transform(GraphInterface graph) {
		Graph newGraph = (Graph) graph.clone();
		Iterator i = newGraph.getEdgesIterator();
		while (i.hasNext()){
			Edge e = (Edge) i.next();
			if (!edgeFilter.isAssignableFrom(e.getContents().getClass())){
							i.remove();
			}
		}
		return newGraph;
	}
}
