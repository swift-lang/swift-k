//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 4, 2005
 */
package org.globus.cog.karajan.workflow.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Queue;
import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentLinkedQueue;

public class Queues {
	private final Queue queues[];
	private int crtqueue;
	private final int[] maxqe;
	private final Object lock = new Object();
	private int count;
	private int eventCount;

	public Queues() {
		queues = new Queue[Priority.ALL.length];
		maxqe = new int[queues.length];
		count = 0;
		for (int i = 0; i < queues.length; i++) {
			queues[i] = new ConcurrentLinkedQueue();
			maxqe[i] = getMaxEvents(Priority.ALL[i].getNumeric());
		}
	}

	private int getMaxEvents(int priority) {
		return (Priority.MAX - priority + 2) * 3;
	}

	public final void enqueue(final EventTargetPair etp) {
		int p = etp.getEvent().getPriority().getNumeric();
		queues[p].add(etp);
		synchronized (lock) {
			count++;
			lock.notify();
		}
	}

	public EventTargetPair nextEvent() throws InterruptedException {
		Object e = stepQueue();
		while (e == null) {
			synchronized (lock) {
				if (count == 0) {
					lock.wait();
				}
			}
			e = stepQueue();
		}
		return (EventTargetPair) e;
	}

	private Object stepQueue() {
		int initial = crtqueue;
		while (queues[crtqueue].isEmpty() || ++eventCount > maxqe[crtqueue]) {
			crtqueue++;
			eventCount = 0;
			if (crtqueue == queues.length) {
				crtqueue = 0;
			}
			if (crtqueue == initial) {
				return null;
			}
		}
		synchronized (lock) {
			count--;
		}
		return queues[crtqueue].poll();
	}

	/**
	 * Must be run with a suspended bus, otherwise the result will be
	 * meaningless
	 */
	public Collection getAll() {
		List l = new ArrayList();
		for (int i = 0; i < queues.length; i++) {
			l.addAll(queues[i]);
		}
		return l;
	}
}
