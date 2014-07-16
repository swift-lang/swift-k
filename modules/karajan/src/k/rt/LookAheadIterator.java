//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 12, 2008
 */
package k.rt;

import java.util.Iterator;

public final class LookAheadIterator<T> implements Iterator<T> {
	private final Iterator<T> it;
	private T last;

	public LookAheadIterator(Iterator<T> it) {
		this.it = it;
	}

	public boolean hasNext() {
		return last != null || it.hasNext();
	}

	public T next() {
		try {
			if (last != null) {
				return last;
			}
			else {
				return it.next();
			}
		}
		finally {
			last = null;
		}
	}

	public void remove() {
		it.remove();
	}

	public T peek() {
		if (last != null) {
			return last;
		}
		else {
			return last = it.next();
		}
	}
}
