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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Base class for submit queues. It uses a generic throttle value.
 * Progress through the queues is triggered using the {@link step()} method
 * which can either be called during the initial submission or when
 * a task completes (which are the only possible cases when something
 * needs to happen within a queue). The {@link step()} method keeps track of 
 * the number of active tasks and, when called, and when the number 
 * of active tasks is below the throttle value, the first task in the
 * queue is forwarded to the next queue. If there is no next queue, the
 * task is submitted.
 * 
 * @author Mihael Hategan
 *
 */
public class AbstractSubmitQueue implements SubmitQueue {
	private Queue queue;
	private int active;
	private int throttle;

	public AbstractSubmitQueue(int throttle) {
		queue = new ConcurrentLinkedQueue();
		this.throttle = throttle;
	}

	public void queue(NonBlockingSubmit nbs) {
		queue.add(nbs);
		step();
	}

	public void submitCompleted(NonBlockingSubmit old, Exception ex) {
		synchronized (this) {
			active--;
		}
		step();
		old.notifyPreviousQueue(ex);
	}

	protected void step() {
		NonBlockingSubmit nbs = null;
		synchronized (this) {
			if (active < throttle && !isQueueEmpty()) {
				active++;
				nbs = poll();
			}
		}
		if (nbs != null) {
			nbs.nextQueue();
		}
	}
	
	protected final NonBlockingSubmit poll() {
		return (NonBlockingSubmit) queue.poll();
	}
	
	protected final boolean isQueueEmpty() {
		return queue.isEmpty();
	}

	public int getThrottle() {
		return throttle;
	}

	public void setThrottle(int throttle) {
		this.throttle = throttle;
	}

}
