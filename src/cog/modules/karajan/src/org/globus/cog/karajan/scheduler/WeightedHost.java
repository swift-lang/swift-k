//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 21, 2005
 */
package org.globus.cog.karajan.scheduler;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.globus.cog.karajan.util.BoundContact;

public class WeightedHost implements Comparable {
	private BoundContact host;
	private Double score;
	private double tscore;
	private int load;
	private double delayedDelta;
	private int jobThrottle;

	public WeightedHost(BoundContact contact, int jobThrottle) {
		this(contact, 0.0, jobThrottle);
	}

	public WeightedHost(BoundContact contact, double score, int jobThrottle) {
		this(contact, score, 0, jobThrottle);
	}

	public WeightedHost(BoundContact contact, double score, int load, int jobThrottle) {
		this.host = contact;
		setScore(score);
		this.load = load;
		this.jobThrottle = jobThrottle;
	}

	protected void setScore(double score) {
		this.score = new Double(score);
		this.tscore = smooth(score);
	}

	public static final double T = 100;
	public static final double B = 2.0 * Math.log(T) / Math.PI;
	public static final double C = 0.2;

	public double smooth(double score) {
		return Math.exp(B * Math.atan(C * score));
	}

	public final double getScore() {
		return score.doubleValue();
	}

	public final Double getScoreAsDouble() {
		return score;
	}

	public final double getTScore() {
		return tscore;
	}

	public final BoundContact getHost() {
		return host;
	}

	public int getLoad() {
		return load;
	}

	public void setLoad(int load) {
		this.load = load;
	}

	public synchronized void changeLoad(int dl) {
		load += dl;
		if (load < 0) {
			load = 0;
		}
	}

	public boolean equals(Object obj) {
		if (obj instanceof WeightedHost) {
			WeightedHost wh = (WeightedHost) obj;
			return host.equals(wh.host);
		}
		return false;
	}

	public int hashCode() {
		return host.hashCode();
	}

	public static final NumberFormat D4;
	static {
		D4 = DecimalFormat.getInstance();
		D4.setMaximumFractionDigits(3);
		D4.setMinimumFractionDigits(3);
	}

	public String toString() {
		return host.toString() + ":" + D4.format(score) + "(" + D4.format(tscore) + "):" + load
				+ "/" + (int)(jobThrottle * tscore + 2) + (isOverloaded() ? " overloaded" : "");
	}

	public int compareTo(Object o) {
		WeightedHost other = (WeightedHost) o;
		int r = score.compareTo(other.score);
		if (r == 0) {
			// arbitrary ordering on the contact
			return System.identityHashCode(host) - System.identityHashCode(other.host);
		}
		else {
			return r;
		}
	}

	public double getDelayedDelta() {
		return delayedDelta;
	}

	public void setDelayedDelta(double delayedDelta) {
		this.delayedDelta = delayedDelta;
	}

	public boolean isOverloaded() {
		return !(load < jobThrottle * tscore + 2);
	}

	public int getJobThrottle() {
		return jobThrottle;
	}
}