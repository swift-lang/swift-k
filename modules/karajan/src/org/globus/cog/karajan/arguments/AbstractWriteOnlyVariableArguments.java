//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 2, 2005
 */
package org.globus.cog.karajan.arguments;

import java.util.Iterator;
import java.util.List;

public abstract class AbstractWriteOnlyVariableArguments implements VariableArguments {
	private static final String WRO = ": write only channel";

	public final List getAll() {
		throw new UnsupportedOperationException("getAll" + WRO);
	}

	public final Object get(int index) {
		throw new UnsupportedOperationException("get" + WRO);
	}

	public final VariableArguments copy() {
		throw new UnsupportedOperationException("copy" + WRO);
	}

	public final int size() {
		throw new UnsupportedOperationException("size" + WRO);
	}

	public final Iterator iterator() {
		throw new UnsupportedOperationException("iterator" + WRO);
	}

	public final Object[] toArray() {
		throw new UnsupportedOperationException("toArray" + WRO);
	}

	public final Object removeFirst() {
		throw new UnsupportedOperationException("removeFirst" + WRO);
	}

	public final void addListener(VariableArgumentsListener l) {
		throw new UnsupportedOperationException("addListener" + WRO);
	}

	public final void removeListener(VariableArgumentsListener l) {
		throw new UnsupportedOperationException("removeListener" + WRO);
	}

	public final boolean isEmpty() {
		throw new UnsupportedOperationException("isEmpty" + WRO);
	}

	public final VariableArguments butFirst() {
		throw new UnsupportedOperationException("butFirst" + WRO);
	}
	
	public final void set(VariableArguments other) {
		throw new UnsupportedOperationException("set" + WRO);
	}

	public final void set(List vargs) {
		throw new UnsupportedOperationException("set" + WRO);
	}

	public void appendAll(List args) {
		Iterator i = args.iterator();
		while (i.hasNext()) {
			append(i.next());
		}
	}

	public void merge(VariableArguments args) {
		appendAll(args.getAll());
	}
}
