// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 21, 2005
 */
package org.globus.cog.karajan.parser;

import java.util.EmptyStackException;

import org.apache.log4j.Logger;

public class Stack<T> extends java.util.Stack<T> {
	private static final long serialVersionUID = 1686310114363996922L;
	
	private final static Logger logger = Logger.getLogger(Stack.class);
	
	public int mark() {
		return size();
	}

	public void forget(final int mark) {
		while (size() > mark) {
			pop();
		}
	}

	public synchronized String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = size()-1; i >= 0; i--) {
			sb.insert(0, get(i));
			if (i != 0) {
				sb.insert(0, ", ");
			}
			if (sb.length() > 256) {
				sb.insert(0, "..., ");
				break;
			}
		}
		sb.append(']');
		sb.insert(0, '[');
		return sb.toString();
	}	
	
	public void swap() {
		int len = size();
		if (len <2) {
			throw new EmptyStackException();
		}
		T top = get(len - 1);
		set(len - 1, get(len - 2));
		set(len - 2, top);
	}
}
