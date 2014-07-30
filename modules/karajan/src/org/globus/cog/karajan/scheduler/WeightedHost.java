/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import org.apache.log4j.Logger;
import org.globus.cog.karajan.util.BoundContact;

public class WeightedHost implements Comparable<WeightedHost> {

	static final int MINWEIGHT = -10;

	private static final Logger logger = Logger.getLogger(WeightedHost.class);

	private BoundContact host;
	private Double score;
	private double tscore;
	private int load;
	private double delayedDelta;
	private float jobThrottle;
	private long lastUsed;
	private double delayBase;
	private int throttleOverride = -1;

	public WeightedHost(BoundContact contact, float jobThrottle) {
		this(contact, 0.0, jobThrottle);
	}

	public WeightedHost(BoundContact contact, double score, float jobThrottle) {
		this(contact, score, 0, jobThrottle);
	}

	public WeightedHost(BoundContact contact, double score, int load, float jobThrottle) {
		this(contact, score, load, jobThrottle, 2);
	}

	public WeightedHost(BoundContact contact, double score, int load, float jobThrottle, double delayBase) {
		this.host = contact;
		setScore(score);
		this.load = load;
		this.jobThrottle = jobThrottle;
		this.delayBase = delayBase;
	}

	protected void setScore(double score) {
		if (score < MINWEIGHT)
			score = MINWEIGHT;
		this.score = new Double(score);
		this.tscore = computeTScore(score);
	}

	public static final double T = 100;
	public static final double B = 2.0 * Math.log(T) / Math.PI;
	public static final double C = 0.2;

	public static double computeTScore(double score) {
		return Math.exp(B * Math.atan(C * score));
	}

	public final double getScore() {
		return score.doubleValue();
	}

	public final Double getScoreAsDouble() {
		return score;
	}

	public final double getTScore() {
		if (tscore >= 1)
			return tscore;
		if (isOverloaded() != 0)
			return 0;
		else
			return 1;
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
				+ "/" + (int) (maxLoad()) + " overload: " + isOverloaded();
	}

	public int compareTo(WeightedHost other) {
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

	public synchronized int isOverloaded() {
		double ml = maxLoad();
		if (tscore >= 1) {
			// the site is mostly good. permit 1 or more jobs
			// always.
			if (logger.isDebugEnabled()) {
				logger.debug("In load mode. score = " + score + " tscore = " + tscore + ", maxload="
						+ ml);
			}
			// the current load must be strictly smaller than the max load
			// to permit any other jobs
			return load < ml ? 0 : 1;
		}
		else {
			// the site is mostly bad. allow either 1 or 0 jobs
			// based on time.
			long now = System.currentTimeMillis();
			long delay = now - lastUsed;
			long permittedDelay = (long) (Math.pow(delayBase, -(score.doubleValue())) * 100.0);
			boolean overloaded = (delay < permittedDelay);
			int d = (int) (delay - permittedDelay);
			// tscore of -1 will give delay of around
			// 200ms, and will double every time tscore goes
			// down by one (which is once per failed job? roughly?)
			if (logger.isDebugEnabled()) {
				logger.debug("In delay mode. score = " + score + " tscore = " + tscore
						+ ", maxload=" + ml + " delay since last used=" + delay + "ms"
						+ " permitted delay=" + permittedDelay + "ms overloaded=" + overloaded+ " delay-permitted delay="+d);
			}
			if (overloaded) {
				return (int) (delay - permittedDelay);
			}
			else {
				return load < ml ? 0 : 1;
			}
		}
	}

	public float getJobThrottle() {
		return jobThrottle;
	}

	public double maxLoad() {
	    if (throttleOverride >= 0) {
	        return throttleOverride;
	    }
	    else {
	    	return jobThrottle * tscore + 1;
	    }
	}

	public void notifyUsed() {
		lastUsed = System.currentTimeMillis();
	}
	
	public void setThrottleOverride(int throttleOverride) {
	    this.throttleOverride = throttleOverride;
	}

	public static double jobThrottleFromMaxParallelism(double max) {
		return (max - 1) / T;
	}

	public static double initialScoreFromInitialParallelism(double initial, double max) {
		if (initial > max) {
			throw new IllegalArgumentException("initialParallelJobs cannot be greater than maxParallelJobs");
        }
        // jobThrottle * tscore + 1 = initial
        // (max - 1) * tscore / T + 1 = initial;
        // tscore = T * (initial - 1) / (max - 1)
        double score;
        if (max == 1) {
            return 0;
        }
        else {
            double tscore = T * (initial - 1) / (max - 1);
            // tscore = exp(B * Math.atan(C * score));
            // ln(tscore) = B * atan(C * score)
            // tan(ln(tscore) / B) = C * score
            // score = tan(ln(tscore) / B) / C
            return Math.tan(Math.log(tscore) / B) / C;
        }
	}
}
