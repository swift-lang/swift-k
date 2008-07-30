//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 30, 2005
 */
package org.globus.cog.karajan.scheduler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.globus.cog.karajan.util.BoundContact;

public class WeightedHostSet {
	private TreeSet scores;
	private Map weightedHosts;
	private double sum;
	private double scoreHighCap;
	private Set overloaded;
	private OverloadedHostMonitor monitor;

	public WeightedHostSet(double scoreHighCap) {
		this(scoreHighCap, null);
	}

	public WeightedHostSet(double scoreHighCap, OverloadedHostMonitor monitor) {
		init();
		this.scoreHighCap = scoreHighCap;
		this.monitor = monitor;
	}

	protected void init() {
		scores = new TreeSet();
		weightedHosts = new HashMap();
		overloaded = new HashSet();
		sum = 0;
	}

	public void add(WeightedHost wh) {
		scores.add(wh);
		weightedHosts.put(wh.getHost(), wh);
		sum += wh.getTScore();
		checkOverloaded(wh);
	}

	public void changeScore(WeightedHost wh, double newScore) {
		scores.remove(wh);
		sum -= wh.getTScore();
		wh.setScore(newScore);
		weightedHosts.put(wh.getHost(), wh);
		scores.add(wh);
		sum += wh.getTScore();
		checkOverloaded(wh);
	}

	public void changeLoad(WeightedHost wh, int dl) {
		wh.changeLoad(dl);
		checkOverloaded(wh);
	}

	public double remove(WeightedHost wh) {
		scores.remove(wh);
		weightedHosts.remove(wh.getHost());
		sum -= wh.getScore();
		removeOverloaded(wh);
		return wh.getScore();
	}

	private void checkOverloaded(WeightedHost wh) {
		int v = wh.isOverloaded();
		if (v > 0) {
			// overloaded either too many tasks (v == 1) or delay already expired (v > 0)
			// there's a bit of ambiguity there, but it does not make a difference
			addOverloaded(wh);
		}
		else if (v < 0) {
			System.err.println(wh.getHost() + " : " + v);
			if (monitor != null) {
				monitor.add(wh);
			}
			addOverloaded(wh);
		}
		else {
			removeOverloaded(wh);
		}
	}

	public WeightedHost findHost(BoundContact bc) {
		return (WeightedHost) weightedHosts.get(bc);
	}

	public Iterator iterator() {
		final Iterator it = scores.iterator();
		return new Iterator() {
			private WeightedHost last;

			public boolean hasNext() {
				return it.hasNext();
			}

			public Object next() {
				return last = (WeightedHost) it.next();
			}

			public void remove() {
				it.remove();
				sum -= last.getScore();
				weightedHosts.remove(last.getHost());
			}
		};
	}

	public WeightedHost last() {
		return (WeightedHost) scores.last();
	}

	public int size() {
		return scores.size();
	}

	public double getSum() {
		return sum;
	}

	public boolean isEmpty() {
		return scores.isEmpty();
	}

	public String toString() {
		return scores.toString();
	}

	public boolean allOverloaded() {
		synchronized(overloaded) {
			return overloaded.size() == weightedHosts.size();
		}
	}

	protected void addOverloaded(WeightedHost wh) {
		synchronized(overloaded) {
			overloaded.add(wh);
		}
	}
	
	protected void removeOverloaded(WeightedHost wh) {
		synchronized(overloaded) {
			overloaded.remove(wh);
		}
	}
}
