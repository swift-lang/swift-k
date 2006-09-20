// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 21, 2005
 */
package org.globus.cog.karajan.util;

import java.util.StringTokenizer;

import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;

public class ThreadingContext {
	private int id;
	private ThreadingContext prev;

	public ThreadingContext() {
		this(null, 0);
	}

	private ThreadingContext(ThreadingContext prev, int id) {
		this.prev = prev;
		this.id = id;
	}

	public ThreadingContext split(int id) {
		ThreadingContext other = new ThreadingContext(this, id);
		return other;
	}

	public boolean equals(Object other) {
		if (other instanceof ThreadingContext) {
			ThreadingContext tc = (ThreadingContext) other;
			if (id != tc.id) {
				return false;
			}
			if (prev == null) {
				return tc.prev == null;
			}
			else
				return prev.equals(tc.prev);
		}
		return false;
	}

	/**
	 * Returns true if
	 * 
	 * @param reference
	 *            is a sub context of this context
	 */
	public boolean isSubContext(ThreadingContext reference) {
		ThreadingContext crt = this;
		while (crt != null) {
			if (crt.equals(reference)) {
				return true;
			}
			crt = crt.prev;
		}
		return false;
	}

	public int hashCode() {
		return prev == null ? id : id + prev.hashCode();
	}

	public String toString() {
		return prev == null ? String.valueOf(id) : prev.toString() + "-" + id;
	}

	public static ThreadingContext parse(String stc) {
		ThreadingContext tc = new ThreadingContext();
		StringTokenizer st = new StringTokenizer(stc, "-");
		while (st.hasMoreTokens()) {
			tc = tc.split(Integer.parseInt(st.nextToken()));
		}
		return tc;
	}

	public static ThreadingContext get(VariableStack stack) throws VariableNotFoundException {
		return (ThreadingContext) stack.getVar("#thread");
	}

	public static void set(VariableStack stack, ThreadingContext context) {
		stack.setVar("#thread", context);
	}

	public int getLastID() {
		return id;
	}
}