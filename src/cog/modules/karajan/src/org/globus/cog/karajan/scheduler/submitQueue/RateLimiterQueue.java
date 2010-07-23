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

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.karajan.util.BoundContact;

/**
 * An implementation of an adaptive rate queue. The rate is decreased
 * as long as a certain condition occurs (typically the task fails 
 * with a specific error). Failed tasks that match the rate decrease
 * condition are re-submitted up to a specific amount of times
 * ({@link #DEFAULT_MAX_RETRIES}). 
 * 
 * @author Mihael Hategan
 *
 */
public class RateLimiterQueue extends AbstractSubmitQueue implements StatusListener {
	public static final Logger logger = Logger.getLogger(RateLimiterQueue.class);

	public static final int DEFAULT_MAX_RETRIES = 2;

	private long lastSubmit, delay;
	private static Timer timer;
	private String errorRegexp;
	private int maxRetries = DEFAULT_MAX_RETRIES;
	private BoundContact contact;

	public RateLimiterQueue(int initialRate, int maxRetries, String errorRegexp,
			BoundContact contact) {
		super(initialRate);
		setRate(initialRate);
		this.errorRegexp = errorRegexp;
		this.maxRetries = maxRetries;
		this.contact = contact;
	}

	public void setRate(int rate) {
		if (rate == 0) {
			throw new IllegalArgumentException("The submission rate must be greater than 0");
		}
		setThrottle(rate);
		delay = (1000 / rate);
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
			nbs.getTask().addStatusListener(this);
			nbs.nextQueue();
		}
	}

	public void submitCompleted(NonBlockingSubmit old, Exception ex) {
		if ((old.getAttempts() <= maxRetries + 1) && ex != null && ex.getMessage() != null
				&& ex.getMessage().matches(errorRegexp)) {
			System.err.println(ex.getMessage());
			// problem triggered
			// decrease rate
			if (getThrottle() > 1) {
				setRate(getThrottle() - 1);
			}
			// resubmit
			queue(old);
		}
		else {
			// not my stuff
			super.submitCompleted(old, ex);
		}
	}

	public void statusChanged(StatusEvent event) {
		Status s = event.getStatus();
		Exception ex = s.getException();
		if (s.getStatusCode() == Status.FAILED && ex != null && ex.getMessage() != null
				&& ex.getMessage().matches(errorRegexp)) {
			if (getThrottle() > 2) {
				setRate(getThrottle() - 1);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("New rate for \"" + contact + "\": " + getThrottle() + " S/s");
			}
		}
	}
}
