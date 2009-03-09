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
import java.util.LinkedList;

import edu.emory.mathcs.backport.java.util.Queue;
import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentLinkedQueue;

public final class WorkerManager {
	public static final int DEFAULT_WORKER_COUNT = Runtime.getRuntime().availableProcessors() * 4;

	private final ConcurrentLinkedQueue idle;
	private final WorkerSet working;
	private final WorkerSweeper sweeper;
	private int defaultWorkerCount = DEFAULT_WORKER_COUNT;

	public WorkerManager() {
		idle = new ConcurrentLinkedQueue();
		working = new WorkerSet();
		for (int i = 0; i < defaultWorkerCount; i++) {
			EventWorker worker = new EventWorker(this);
			worker.start();
			idle.add(worker);
		}
		sweeper = new WorkerSweeper(this);
	}

	public EventWorker reserveWorker() throws InterruptedException {
		final EventWorker worker;
		synchronized (this) {
			while (idle.isEmpty()) {
				wait();
			}
		}
		worker = (EventWorker) idle.poll();
		working.add(worker);
		return worker;
	}

	public void releaseWorker(final EventWorker worker) {
		working.remove(worker);
		if (worker.isMarkedForRemoval()) {
			worker.shutdown();
		}
		else {
			idle.add(worker);
			synchronized (this) {
				notify();
			}
		}
	}

	public void addWorker() {
		EventWorker worker = new EventWorker(this);
		worker.start();
		idle.add(worker);
		synchronized (this) {
			notify();
		}
	}

	public Queue getIdle() {
		return idle;
	}

	/*
	 * public synchronized List getWorking() { return new LinkedList(working); }
	 */

	public synchronized Collection getWorking() {
		return working.getAll();
	}

	public int getDefaultWorkerCount() {
		return defaultWorkerCount;
	}

	public void setDefaultWorkerCount(final int defaultWorkerCount) {
		this.defaultWorkerCount = defaultWorkerCount;
	}

	public static final class WorkerSet {
		private final EventWorker[] table;
		private int size;

		public WorkerSet() {
			table = new EventWorker[EventWorker.MAXID + 1];
		}

		public int size() {
			return size;
		}

		public boolean isEmpty() {
			return size == 0;
		}

		public boolean contains(final EventWorker o) {
			return table[o.getID()] != null;
		}

		public void add(final EventWorker o) {
			if (table[o.getID()] == null) {
				size++;
			}
			table[o.getID()] = o;
		}

		public void remove(final EventWorker o) {
			if (table[o.getID()] != null) {
				size--;
			}
			table[o.getID()] = null;
		}

		public LinkedList getAll() {
			LinkedList l = new LinkedList();
			for (int i = 0; i < table.length; i++) {
				Object o = table[i];
				if (o != null) {
					l.add(o);
				}
			}
			return l;
		}
	}
}
