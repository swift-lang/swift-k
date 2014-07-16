//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 12, 2012
 */
package k.rt;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractFuture implements Future {
	private LinkedList<FutureListener> listeners;
	
	protected abstract boolean isClosed();
	
	@Override
	public void addListener(FutureListener l, ConditionalYield y) {
		boolean closed;
		synchronized(this) {
			if (listeners == null) {
				listeners = new LinkedList<FutureListener>();
			}
			listeners.add(l);
			closed = isClosed();
		}
		if (closed) {
			notifyListeners();
		}
	}
	
	protected void notifyListeners() {
		List<FutureListener> ls;
		synchronized(this) {
			if (listeners == null) {
				return;
			}
			ls = listeners;
			listeners = null;
		}
		for (FutureListener l : ls) {
			l.futureUpdated(this);
		}
	}
}
