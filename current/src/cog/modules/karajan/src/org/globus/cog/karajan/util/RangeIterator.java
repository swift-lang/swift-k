// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 18, 2004
 */
package org.globus.cog.karajan.util;

import java.util.NoSuchElementException;

public class RangeIterator implements KarajanIterator {
	private final int start, end, step;
	private int crt;

	public RangeIterator(int start, int end, int step) {
		if (step == 0) {
			throw new IllegalArgumentException("Step must be greater than 0");
		}
		this.start = start;
		this.end = end;
		this.crt = start;
		this.step = step;
	}

	public RangeIterator(int start, int end) {
		this(start, end, 1);
	}

	public int count() {
		return (end - start) / step + 1;
	}

	public int remaining() {
		return (end - crt) / step + 1;
	}

	public int current() {
		return crt;
	}

	public void remove() {
		throw new RuntimeException("Unsupported operation");
	}

	public boolean hasNext() {
		return crt <= end;
	}

	public Object next() {
		if (crt > end) {
			throw new NoSuchElementException();
		}
		try {
			return new Double(crt);
		}
		finally {
			crt += step;
		}
	}

	public Object peek() {
		return new Double(crt);
	}

	public boolean isClosed() {
		return false;
	}

	public String toString() {
		if (step != 1) {
			return "[" + start + " .. " + end + " step " + step + "]";
		}
		else {
			return "[" + start + " .. " + end + "]";
		}
	}
}
