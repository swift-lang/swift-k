
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.util.graph;

public class NodeNotFoundException extends GraphException {

    public NodeNotFoundException() {
        super("Node not found");
    }

	public NodeNotFoundException(String msg) {
		super(msg);
	}
}
