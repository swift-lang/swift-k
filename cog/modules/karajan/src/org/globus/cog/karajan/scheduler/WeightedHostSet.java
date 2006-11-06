//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 30, 2005
 */
package org.globus.cog.karajan.scheduler;

import java.util.Iterator;
import java.util.TreeSet;

public class WeightedHostSet {
	private TreeSet scores;
	private double sum;

	public WeightedHostSet() {
		init();
	}
	
	protected void init() {
		scores = new TreeSet();
		sum = 0;
	}

	public synchronized void add(WeightedHost wh) {
		scores.add(wh);
		sum += wh.getScore();
	}
	
	public synchronized void changeScore(WeightedHost wh, double newScore) {
		scores.remove(wh);
		sum -= wh.getScore();
		scores.add(new WeightedHost(wh.getHost(), newScore));
		sum += newScore;
	}

	public synchronized double remove(WeightedHost wh) {
		scores.remove(wh);
		return wh.getScore();
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
