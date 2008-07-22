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
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.globus.cog.karajan.util.BoundContact;

public class WeightedHostSet {
	private TreeSet scores;
	private Map weightedHosts;
	private double sum;
	private double scoreHighCap;
	private volatile int overloadedCount;
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
		sum = 0;
	}

	public void add(WeightedHost wh) {
		scores.add(wh);
		weightedHosts.put(wh.getHost(), wh);
		sum += wh.getTScore();
		checkOverloaded(wh, 1);
	}

	public void changeScore(WeightedHost wh, double newScore) {
		scores.remove(wh);
		sum -= wh.getTScore();
		checkOverloaded(wh, -1);
		wh.setScore(newScore);
		weightedHosts.put(wh.getHost(), wh);
		scores.add(wh);
		sum += wh.getTScore();
		checkOverloaded(wh, 1);
	}

	public void changeLoad(WeightedHost wh, int dl) {
		checkOverloaded(wh, -1);
		wh.changeLoad(dl);
		checkOverloaded(wh, 1);
	}

	public double remove(WeightedHost wh) {
		scores.remove(wh);
		weightedHosts.remove(wh.getHost());
		sum -= wh.getScore();
		checkOverloaded(wh, -1);
		return wh.getScore();
	}

	private void checkOverloaded(WeightedHost wh, final int dir) {
		int v = wh.isOverloaded();
		int countDelta;
		if (v == 0) {
			// not overloaded
			countDelta = 0;
		}
		else if (v > 0) {
			// overloaded either too many tasks (v == 1) or delay already expired (v > 0)
			// there's a bit of ambiguity there, but it does not make a difference
			if (dir > 0) {
				countDelta = v;
			}
			else {
				countDelta = -v;
			}
		}
		else {
			if (monitor != null) {
				monitor.add(wh, -dir);
			}
			countDelta = dir;
		}
		updateOverloadedCount(countDelta);
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
		return overloadedCount == weightedHosts.size();
	}

	protected void updateOverloadedCount(int dif) {
		if (dif != 0) {
			this.overloadedCount += dif;
		}
	}
}
