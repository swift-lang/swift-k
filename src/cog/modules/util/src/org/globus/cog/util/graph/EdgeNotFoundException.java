
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.util.graph;

/**
 * Thrown by operations involving an edge when the edge does not actually exist
 * in the graph
 */
public class EdgeNotFoundException extends GraphException {

    /**
	 * Constructs a new <code>EdgeNotFoundException</code>
	 */
	public EdgeNotFoundException() {
        super("Edge not found");
    }
}
