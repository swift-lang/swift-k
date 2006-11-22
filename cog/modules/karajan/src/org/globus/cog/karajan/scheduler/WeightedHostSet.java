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

	public WeightedHostSet() {
		init();
	}
	
	protected void init() {
		scores = new TreeSet();
		weightedHosts = new HashMap();
		sum = 0;
	}

	public synchronized void add(WeightedHost wh) {
		scores.add(wh);
		weightedHosts.put(wh.getHost(), wh);
		sum += wh.getScore();
	}
	
	public synchronized void changeScore(WeightedHost wh, double newScore) {
		scores.remove(wh);
		sum -= wh.getScore();
		WeightedHost nwh = new WeightedHost(wh.getHost(), newScore);
		weightedHosts.put(wh.getHost(), nwh);
		scores.add(nwh);
		sum += newScore;
	}

	public synchronized double remove(WeightedHost wh) {
		scores.remove(wh);
		weightedHosts.remove(wh.getHost());
		return wh.getScore();
	}
	
	public WeightedHost findHost(BoundContact bc) {
		return (WeightedHost) weightedHosts.get(bc);
	}

	public Iterator iterator() {
		return scores.iterator(); 
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

	protected synchronized void normalize(double target) {
		double prod = 1;
		Iterator i = scores.iterator();
		while (i.hasNext()) {
			WeightedHost wh = (WeightedHost) i.next();
			prod *= wh.getScore();
		}
		double geomAvg = Math.pow(prod, target / scores.size());
		double renormalizationFactor = 1 / geomAvg;
		i = scores.iterator();
		scores = new TreeSet();
		while (i.hasNext()) {
			WeightedHost wh = (WeightedHost) i.next();
			WeightedHost nwh = new WeightedHost(wh.getHost(), wh.getScore()
					* renormalizationFactor);
			add(nwh);
		}
	}

	public String toString() {
		return scores.toString();
	}
}
