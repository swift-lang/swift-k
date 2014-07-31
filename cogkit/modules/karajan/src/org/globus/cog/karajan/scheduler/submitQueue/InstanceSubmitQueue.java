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
