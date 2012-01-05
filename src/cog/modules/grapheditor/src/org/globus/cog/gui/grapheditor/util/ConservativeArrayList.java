
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 6, 2003
 *
 */
package org.globus.cog.gui.grapheditor.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Provides a memory conservative that only allocates
 * an array of the needed size. 
 */
public class ConservativeArrayList extends ArrayList{
	
	
	/* (non-Javadoc)
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public void add(int index, Object element) {
		super.add(index, element);
		trimToSize();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	public boolean add(Object o) {
		boolean b = super.add(o);
		trimToSize();
		return b;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection c) {
		boolean b = super.addAll(c);
		trimToSize();
		return b;
	}

	/* (non-Javadoc)
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection c) {
		boolean b = super.addAll(index, c);
		trimToSize();
		return b;
	}

	/* (non-Javadoc)
	 * @see java.util.List#remove(int)
	 */
	public Object remove(int index) {
		Object o = super.remove(index);
		trimToSize();
		return o;
	}

	/**
	 * 
	 */
	public ConservativeArrayList() {
		super();
	}

	/**
	 * @param initialCapacity
	 */
	public ConservativeArrayList(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * @param c
	 */
	public ConservativeArrayList(Collection c) {
		super(c);
	}

}
