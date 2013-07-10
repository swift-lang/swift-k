
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

public class NodeItr implements NodeIterator {
	Iterator iterator;

	protected NodeItr(Iterator i) {
		this.iterator = i;
	}

	public boolean hasNext() {
		return iterator.hasNext();
	}

	public Object next() {
		return iterator.next();
	}

	public boolean hasMoreNodes() {
		return iterator.hasNext();
	}

	public Node nextNode() {
		return (Node) iterator.next();
	}

	public void remove() {
		iterator.remove();
	}

}
