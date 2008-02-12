//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 21, 2006
 */
package org.globus.cog.karajan.scheduler.submitQueue;

import java.util.HashMap;
import java.util.Map;

import org.globus.cog.karajan.util.BoundContact;

public class HostSubmitQueue extends AbstractSubmitQueue {
	private Map providerQueues;
	private BoundContact contact;
	private FixedRateQueue rlq;

	public HostSubmitQueue(BoundContact contact, int throttle) {
		super(throttle);
		this.contact = contact;
	}

	private static final NullQueue NULL_QUEUE = new NullQueue();

	public synchronized SubmitQueue getProviderQueue(String provider, int initialRate,
			int maxRetries, String errorRegexp) {
		if (providerQueues == null) {
			providerQueues = new HashMap();
		}
		SubmitQueue sq;
		if ("ssh".equalsIgnoreCase(provider)) {
			sq = (SubmitQueue) providerQueues.get(provider.toLowerCase());
			if (sq == null) {
				sq = new RateLimiterQueue(initialRate, maxRetries, errorRegexp, contact);
				providerQueues.put(provider, sq);
			}
		}
		else if (contact.getProperty("maxSubmitRate") != null) {
			if (rlq == null) {
				rlq = new FixedRateQueue(Double.parseDouble(contact.getProperty("maxSubmitRate").toString()));
			}
			sq = rlq;
		}
		else {
			sq = NULL_QUEUE;
		}
		return sq;
	}
}
