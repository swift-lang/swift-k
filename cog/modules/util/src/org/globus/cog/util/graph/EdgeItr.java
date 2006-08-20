
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

public class EdgeItr implements EdgeIterator {
	private Iterator iterator;
	
	protected EdgeItr() {
	}

	protected EdgeItr(Iterator i) {
		this.iterator = i;
	}

	public boolean hasNext() {
		if (iterator == null) {
			return false;
		}
		return iterator.hasNext();
	}

	public Object next() {
		if (iterator == null) {
			return null;
		}
		return iterator.next();
	}

	public boolean hasMoreEdges() {
		if (iterator == null) {
			return false;
		}
		return iterator.hasNext();
	}

	public Edge nextEdge() {
		if (iterator == null) {
			return null;
		}
		return (Edge) iterator.next();
	}

	public void remove() {
		if (iterator == null) {
			return;
		}
		iterator.remove();
	}
}