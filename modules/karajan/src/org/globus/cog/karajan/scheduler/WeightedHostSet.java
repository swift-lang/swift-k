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

public class WeightedHostSet implements Iterable<WeightedHost> {
	private TreeSet<WeightedHost> scores;
	private Map<BoundContact, WeightedHost> weightedHosts;
	private double sum;
	private double scoreHighCap;
	private Set<WeightedHost> overloaded;
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
		scores = new TreeSet<WeightedHost>();
		weightedHosts = new HashMap<BoundContact, WeightedHost>();
		overloaded = new HashSet<WeightedHost>();
		sum = 0;
	}

	public void add(WeightedHost wh) {
		scores.add(wh);
		weightedHosts.put(wh.getHost(), wh);
		sum += wh.getTScore();
		checkOverloaded(wh);
	}

	public void changeScore(WeightedHost wh, double newScore) {
		synchronized (scores) {
			scores.remove(wh);
			sum -= wh.getTScore();
			wh.setScore(newScore);
			weightedHosts.put(wh.getHost(), wh);
			scores.add(wh);
			sum += wh.getTScore();
			checkOverloaded(wh);
		}
	}
	
	public double changeScoreDelta(WeightedHost wh, double delta) {
		synchronized (scores) {
			double old = wh.getScore();
			scores.remove(wh);
			sum -= wh.getTScore();
			wh.setScore(old + delta);
			weightedHosts.put(wh.getHost(), wh);
			scores.add(wh);
			sum += wh.getTScore();
			checkOverloaded(wh);
			return old + delta;
		}
	}

	public void changeLoad(WeightedHost wh, int dl) {
		wh.changeLoad(dl);
		checkOverloaded(wh);
	}
	
	public void changeThrottleOverride(WeightedHost wh, int throttleOverride) {
	    synchronized(scores) {
	        wh.setThrottleOverride(throttleOverride);
	        checkOverloaded(wh);
	    }
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
		return weightedHosts.get(bc);
	}
	
	public WeightedHostSet constrain(ResourceConstraintChecker rcc, TaskConstraints tc) {
        WeightedHostSet ns = new WeightedHostSet(scoreHighCap);
        synchronized (scores) {
        	for (WeightedHost wh : scores) {
        		if (rcc.checkConstraints(wh.getHost(), tc)) {
        			ns.add(wh);
        		}
        	}
        	return ns;
        }
	}

	/**
	 * Warning: objects returned by this method are not synchronized. It is
	 * therefore possible for some hosts to be missed due to threads calling
	 * changeScore() (which temporarily removes hosts from the set).
	 */
	public Iterator<WeightedHost> iterator() {
		final Iterator<WeightedHost> it = scores.iterator();
		return new Iterator<WeightedHost>() {
			private WeightedHost last;

			public boolean hasNext() {
				return it.hasNext();
			}

			public WeightedHost next() {
				return last = it.next();
			}

			public void remove() {
				it.remove();
				sum -= last.getScore();
				weightedHosts.remove(last.getHost());
			}
		};
	}

	public WeightedHost last() {
		return scores.last();
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
