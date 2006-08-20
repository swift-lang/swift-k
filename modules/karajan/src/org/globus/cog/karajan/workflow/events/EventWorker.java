//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 23, 2005
 */
package org.globus.cog.karajan.workflow.events;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

public final class EventWorker extends Thread {
	private static final Logger logger = Logger.getLogger(EventWorker.class);

	public static final int MAXID = 127;

	private final WorkerManager manager;
	private long start;
	private static int sid = 0;
	private static Set ids = new HashSet();
	private int id;
	private EventTargetPair etp;
	private boolean markedForRemoval;
	private boolean done;
	public static volatile long eventsDispatched;

	public EventWorker(WorkerManager manager) {
		this.manager = manager;
		synchronized (EventWorker.class) {
			do {
				id = (sid++) % MAXID;
			}
			while (ids.contains(new Integer(id)));
			ids.add(new Integer(id));
		}
		setName("Worker " + id);
		start = Long.MAX_VALUE;
		setDaemon(true);
	}

	public void dispatch(EventTargetPair pair) {
		synchronized (this) {
			etp = pair;
			notify();
		}
	}

	public void run() {
		while (!done) {
			synchronized (this) {
				while (etp == null) {
					try {
						wait();
					}
					catch (InterruptedException e) {
						return;
					}
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug(etp.getEvent() + " -> " + etp.getTarget());
			}
			start = System.currentTimeMillis();
			EventBus.sendHooked(etp.getTarget(), etp.getEvent());
			EventBus.cummulativeEventTime += System.currentTimeMillis() - start;
			start = Long.MAX_VALUE;
			etp = null;
			eventsDispatched++;
			manager.releaseWorker(this);
		}
	}

	public int getID() {
		return id;
	}

	public long getStart() {
		return start;
	}

	public void markForRemoval() {
		this.markedForRemoval = true;
	}

	public boolean isMarkedForRemoval() {
		return markedForRemoval;
	}

	public void shutdown() {
		done = true;
	}

	public String toString() {
		if (start == Long.MAX_VALUE) {
			return "Worker(" + id + ")[idle, etp=" + etp + "]";
		}
		else {
			return "Worker(" + id + ")[" + (System.currentTimeMillis() - start) + "]";
		}
	}
}
