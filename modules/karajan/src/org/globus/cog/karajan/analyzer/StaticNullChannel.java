//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 22, 2012
 */
package org.globus.cog.karajan.analyzer;

import java.util.List;

public class StaticNullChannel extends StaticChannel {
	private int id;
	private static int sid;
	
	public StaticNullChannel() {
		this.id = sid++;
	}
	
	@Override
	public boolean append(Object value) {
		return false;
	}

	@Override
	public boolean appendAll(List<Object> l) {
		return false;
	}

	@Override
	public List<Object> getAll() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}
	
	public String toString() {
		return "<" + (isDynamic() ? "?" : "") + "null" + id + ">";
	}
}
