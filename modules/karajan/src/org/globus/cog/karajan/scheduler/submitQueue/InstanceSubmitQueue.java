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

/**
 * A plain throttling queue. Each scheduler instance should have one
 * instance of this queue, typically chained after the global submit
 * queue and before host submit queues.
 * 
 * @author Mihael Hategan
 *
 */
public class InstanceSubmitQueue extends AbstractSubmitQueue {
	private Map queues;
	private int hostThrottle = 8;

	public InstanceSubmitQueue() {
		super(16);
		queues = new HashMap();
	}

	public HostSubmitQueue getHostQueue(BoundContact contact) {
		synchronized (queues) {
			HostSubmitQueue hq = (HostSubmitQueue) queues.get(contact);
			if (hq == null) {
				hq = new HostSubmitQueue(contact, hostThrottle);
				queues.put(contact, hq);
			}
			return hq;
		}
	}

	public int getHostThrottle() {
		return hostThrottle;
	}

	public void setHostThrottle(int hostThrottle) {
		this.hostThrottle = hostThrottle;
	}
}
