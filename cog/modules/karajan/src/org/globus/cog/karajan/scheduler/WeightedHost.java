//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 21, 2005
 */
package org.globus.cog.karajan.scheduler;

import org.globus.cog.karajan.util.BoundContact;

public class WeightedHost implements Comparable {
	private BoundContact host;
	private Double score;

	public WeightedHost(BoundContact contact) {
		this(contact, 1.0);
	}

	public WeightedHost(BoundContact contact, double score) {
		this.host = contact;
		this.score = new Double(score);
	}

	public final double getScore() {
		return score.doubleValue();
	}
	
	public final Double getScoreAsDouble() {
		return score;
	}
	
	public final BoundContact getHost() {
		return host;
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

	public String toString() {
		return host.toString();
	}

	public int compareTo(Object o) {
		WeightedHost other = (WeightedHost) o;
		int r = score.compareTo(other.score);
		if (r == 0) {
			//arbitrary ordering on the contact
			return System.identityHashCode(host) - System.identityHashCode(other.host);
		}
		else {
			return r;
		}
	}
}