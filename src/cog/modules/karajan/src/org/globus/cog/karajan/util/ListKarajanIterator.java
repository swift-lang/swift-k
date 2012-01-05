// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 18, 2004
 */
package org.globus.cog.karajan.util;

import java.util.List;


public class ListKarajanIterator extends AbstractKarajanIterator {
	private final List list;

	public ListKarajanIterator(List l) {
		super(l.iterator());
		this.list = l;
	}

	public int count() {
		return list.size();
	}

	public List getList() {
		return list;
	}
	
	public void reset() {
		setIterator(list.iterator());
	}
}