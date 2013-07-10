//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2006
 */
package org.globus.cog.karajan.scheduler.submitQueue;

import java.util.Timer;
import java.util.TimerTask;

import org.globus.cog.abstraction.interfaces.Task;

/**
 * A submit queue which enforces a minimum time delay between
 * subsequent tasks. 
 * 
 * @author Mihael Hategan
 *
 */
public class FixedRateQueue extends AbstractSubmitQueue {
	private long lastSubmit, delay;
	private static Timer timer;

	public FixedRateQueue(long delay) {
		super(Math.max(1, (int) (delay / 1000)));
		this.delay = delay;
	}

	public FixedRateQueue(double rate) {
		super(Math.max(1, (int) rate));
		this.delay = (long) (1000 / rate);
	}

	private static synchronized Timer getTimer() {
		if (timer == null) {
			timer = new Timer(true);
		}
		return timer;
	}

	protected void step() {
		NonBlockingSubmit nbs = null;
		synchronized (this) {
			if (!isQueueEmpty()) {
				long time = System.currentTimeMillis();
				if (time - lastSubmit > delay) {
					nbs = poll();
					lastSubmit = time;
				}
				else {
					getTimer().schedule(new TimerTask() {
						public void run() {
							step();
						}
					}, delay - time + lastSubmit);
				}
			}
		}
		if (nbs != null) {
			nbs.nextQueue();
		}
	}

	public void queue(NonBlockingSubmit nbs) {
		if (nbs != null) {
			if (nbs.getTask().getType() == Task.JOB_SUBMISSION) {
				super.queue(nbs);
			}
			else {
				nbs.nextQueue();
			}
		}
		else {
			super.queue(nbs);
		}
	}
}
