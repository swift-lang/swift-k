/*
 * Created on Jun 9, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.AbstractList;

public class Pair extends AbstractList {
	private Object[] elements = new Object[2];

	public Pair(Object o1, Object o2) {
		elements[0] = o1;
		elements[1] = o2;
	}

	public Object get(int index) {
		return elements[index];
	}

	public int size() {
		return 2;
	}

}
