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
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import org.globus.cog.karajan.util.BoundContact;

public class WeightedHostSet {
	private TreeSet scores;
	private Map weightedHosts;
	private double sum;
	private double scoreHighCap;
	private volatile int overloadedCount;

	private static final Timer timer = new Timer();

	public WeightedHostSet(double scoreHighCap) {
		init();
		this.scoreHighCap = scoreHighCap;
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
		overloadedCount += checkOverloaded(wh, 1);
	}

	public void changeScore(WeightedHost wh, double newScore) {
		scores.remove(wh);
		sum -= wh.getTScore();
		overloadedCount += checkOverloaded(wh, -1);
		wh.setScore(newScore);
		weightedHosts.put(wh.getHost(), wh);
		scores.add(wh);
		sum += wh.getTScore();
		overloadedCount += checkOverloaded(wh, 1);
	}

	public void changeLoad(WeightedHost wh, int dl) {
		overloadedCount -= checkOverloaded(wh, -1);
		wh.changeLoad(dl);
		overloadedCount += checkOverloaded(wh, 1);
	}

	public double remove(WeightedHost wh) {
		scores.remove(wh);
		weightedHosts.remove(wh.getHost());
		sum -= wh.getScore();
		overloadedCount += checkOverloaded(wh, -1);
		return wh.getScore();
	}

	private int checkOverloaded(WeightedHost wh, final int dir) {
		int v = wh.isOverloaded();
		if (v >= 0) {
		    if (dir > 0) {
		    	return v;
		    }
		    else {
		        return -v;
		    }
		}
		else {
			timer.schedule(new TimerTask() {
				public void run() {
				    overloadedCount -= dir;
				}
			}, -v);
			return dir;
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
		return overloadedCount == weightedHosts.size();
	}
}
