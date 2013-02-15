//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 16, 2005
 */
package org.globus.cog.karajan.util;

import java.util.Iterator;


public abstract class AbstractKarajanIterator implements KarajanIterator {
	private Iterator iterator;
	private int crt;
	private Object next;
	private boolean nextValid;
	
	public AbstractKarajanIterator(Iterator iterator) {
		this.iterator = iterator;
	}
	
	public boolean hasNext() {
		return nextValid || iterator.hasNext();
	}

	public Object next() {
		if (nextValid) {
			nextValid = false;
			crt++;
			return next;
		}
		else {
			if (iterator.hasNext()) {
				crt++;
			}
			return iterator.next();	
		}
	}
	
	public Object peek() {
		if (nextValid) {
			return next;
		}
		else {
			next = iterator.next();
			nextValid = true;
			return next;
		}
	}

	public void remove() {
		iterator.remove();
	}
	
	protected void setIterator(Iterator i) {
		this.iterator = i;
		crt = 0;
	}
	
	public int current() {
		return crt;
	}
	
	public int remaining() {
		return count() - current();
	}
	
	public abstract void reset();
	
	public void skipTo(int current) {
		while (current() < current) {
			next();
		}
	}
}
