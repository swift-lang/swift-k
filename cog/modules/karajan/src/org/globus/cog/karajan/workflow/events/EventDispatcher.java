//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 23, 2005
 */
package org.globus.cog.karajan.workflow.events;

import java.util.Collection;

import org.apache.log4j.Logger;

public class EventDispatcher extends Thread {
	private static final Logger logger = Logger.getLogger(EventDispatcher.class);

	private final Queues events;
	private final WorkerManager workers;
	private boolean suspend, suspended;

	public static EventDispatcher newDispatcher(Queues events, WorkerManager workers) {
		return new EventDispatcher(events, workers);
	}

	protected EventDispatcher(Queues events, WorkerManager workers) {
		this.events = events;
		this.workers = workers;
		setName("EventDispatcher");
		setDaemon(true);
	}

	public void run() {
		try {
			while (true) {
				if (!suspend) {
					suspended = false;
					EventTargetPair etp = events.nextEvent();
					EventWorker next = workers.reserveWorker();
					next.dispatch(etp);
				}
				else {
					suspended = true;
					synchronized (this) {
						wait(1000);
					}
				}
			}
		}
		catch (InterruptedException e) {
		}
	}

	public void suspendAll() {
		suspend = true;
	}

	public void resumeAll() {
		suspend = false;
		synchronized (this) {
			notify();
		}
	}

	public boolean isSuspended() {
		return suspended && suspend;
	}

	public Collection getAllEvents() {
		return events.getAll();
	}
}
