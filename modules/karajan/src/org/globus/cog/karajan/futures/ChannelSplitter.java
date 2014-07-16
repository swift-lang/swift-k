//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 6, 2006
 */
package org.globus.cog.karajan.futures;

import k.rt.Future;
import k.rt.FutureListener;
import k.rt.FutureMemoryChannel;

/**
 * This class allows splitting of a channel into multiple copies.
 * This allows the split channels to be used independently (including
 * removing objects from the channel).
 * 
 * @author Mihael Hategan
 *
 */
public class ChannelSplitter<T> implements FutureListener {
	private final FutureMemoryChannel<T> c;
	private final FutureMemoryChannel<T>[] out;
	
	@SuppressWarnings("unchecked")
	public ChannelSplitter(FutureMemoryChannel<T> c, int count) {
		this.c = c;
		out = new FutureMemoryChannel[count];
		for (int i = 0; i < count; i++) {
			out[i] = new FutureMemoryChannel<T>();
		}
		c.addListener(this, null);
	}
	
	public FutureMemoryChannel<T>[] getOuts() {
		return out;
	}

	public void futureUpdated(Future f) {
		while(c.available() > 0) {
			T o = c.removeFirst();
			for (int i = 0; i < out.length; i++) {
				out[i].add(o);
			}
		}
		if (c.isClosed()) {
			for (int i = 0; i < out.length; i++) {
				out[i].close();
			}
		}
	}
}
