//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 6, 2005
 */
package org.globus.cog.karajan.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.arguments.VariableArgumentsListener;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;

/**
 * I know the name contradicts itself.
 */
public class SingleValueVariableArguments implements VariableArguments {
	private Object value;

	public void merge(VariableArguments args) {
		appendAll(args.getAll());
	}

	public void append(Object value) {
		if (this.value != null) {
			throw new KarajanRuntimeException("This channel can only have one value");
		}
		this.value = value;
	}

	public void appendAll(List args) {
		if (args.size() == 0) {
			return;
		}
		else if (args.size() == 1) {
			append(args.get(0));
		}
		else {
			throw new KarajanRuntimeException("This channel can only have one value");
		}
	}

	public List getAll() {
		LinkedList l = new LinkedList();
		if (value != null) {
			l.add(value);
		}
		return l;
	}

	public void set(List vargs) {
		value = null;
		appendAll(vargs);
	}

	public Object get(int index) {
		if (index > 0 || index < 0 || value == null) {
			throw new IndexOutOfBoundsException("Single value channel: " + index);
		}
		return value;
	}

	public VariableArguments copy() {
		return null;
	}

	public int size() {
		if (value == null) {
			return 0;
		}
		else {
			return 1;
		}
	}

	public Iterator iterator() {
		throw new UnsupportedOperationException("iterator()");
	}

	public Object[] toArray() {
		throw new UnsupportedOperationException("toArray()");
	}

	public void set(VariableArguments other) {
		throw new UnsupportedOperationException("set()");
	}

	public Object removeFirst() {
		Object val = get(0);
		value = null;
		return val;
	}

	public void addListener(VariableArgumentsListener l) {
		throw new UnsupportedOperationException("addListener");
	}

	public void removeListener(VariableArgumentsListener l) {
		throw new UnsupportedOperationException("removeListener");
	}

	public String toString() {
		return "[" + (value == null?"?":value.toString())+"]";
	}

	public boolean isEmpty() {
		return value != null;
	}

	public VariableArguments butFirst() {
		throw new UnsupportedOperationException("removeListener");
	}

	public boolean isCommutative() {
		return true;
	}
}
