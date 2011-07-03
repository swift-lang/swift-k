/*
 * Created on Jun 9, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.Iterator;
import java.util.Map;

import org.globus.cog.karajan.util.KarajanIterator;

public class PairIterator implements KarajanIterator {
	private Iterator<?> it;
	private int crt, count;
	private Pair crto;
	
	public PairIterator(Map<?, ?> map) {
		this.it = map.entrySet().iterator();
		this.count = map.size();
	}

	public int current() {
		return crt;
	}

	public int count() {
		return count;
	}

	public Object peek() {
		if (crto == null) {
			crto = convert(it.next());
		}
		return crto;
	}

	public void remove() {
		throw new UnsupportedOperationException("remove");
	}

	public boolean hasNext() {
		return it.hasNext() || crto != null;
	}

	public Object next() {
		crt++;
		if (crto != null) {
			Object o = crto;
			crto = null;
			return o;
		}
		else {
			return convert(it.next());
		}
	}
	
	private Pair convert(Object o) {
		Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
		return new Pair(e.getKey(), e.getValue());
	}
	
}
