
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 17, 2003
 */
package org.globus.cog.util.graph;

import java.util.Iterator;

public interface EdgeIterator extends Iterator{
	public boolean hasMoreEdges();
	
	public Edge nextEdge();
}
