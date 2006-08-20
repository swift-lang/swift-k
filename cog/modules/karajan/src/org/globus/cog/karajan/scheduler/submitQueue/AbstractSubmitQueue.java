//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 21, 2006
 */
package org.globus.cog.karajan.scheduler.submitQueue;

import edu.emory.mathcs.backport.java.util.Queue;
import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentLinkedQueue;

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
