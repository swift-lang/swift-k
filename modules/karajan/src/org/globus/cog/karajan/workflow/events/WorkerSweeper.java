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
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.util.ThreadManager;

public class WorkerSweeper {
	private static final Logger logger = Logger.getLogger(WorkerSweeper.class);

	public static final int DEFAULT_BUS_SWEEPER_CYCLE = 3000;
	public static final int DEFAULT_INITIAL_WAIT = 5000;
	public static final int DEFAULT_REDUCE_CYCLE_COUNT = 4;
	public static final int DEFAULT_MAX_EVENT_TIME = 1000;

	private Timer timer;
	private int cycle = DEFAULT_BUS_SWEEPER_CYCLE;
	private int reduceCycleCount = DEFAULT_REDUCE_CYCLE_COUNT;
	private int initialWait = DEFAULT_INITIAL_WAIT;
	private int maxEventTime = DEFAULT_MAX_EVENT_TIME;
	private int cleanCycles;
	private boolean running;

	private long lastEventCount;

	public static int added, textra;

	private final SweeperTask task;

	private final WorkerManager manager;

	private int extra, reduce;

	public WorkerSweeper(WorkerManager manager) {
		this.manager = manager;
		timer = new Timer(true);
		task = new SweeperTask();
		timer.schedule(task, initialWait, cycle);
		extra = 0;
	}

	public class SweeperTask extends TimerTask {
		public void run() {
			if (lastEventCount == EventBus.eventCount) {
				if (logger.isInfoEnabled()) {
					logger.info("No events in one sweeper cycle");
					if (!logger.isDebugEnabled()) {
						logger.info("Idle busses: " + manager.getIdle().size());
						logger.info("Working busses: " + manager.getWorking().size() + " - "
								+ manager.getWorking());
						logger.info("Total events queued: " + EventBus.eventCount);
						logger.info("Total events dispatched: " + EventWorker.eventsDispatched);
						logger.info("Dispatcher suspended: "
								+ EventBus.getDispatcher().isSuspended());
					}
				}
			}
			lastEventCount = EventBus.eventCount;
			if (logger.isDebugEnabled()) {
				logger.debug("Idle busses: " + manager.getIdle().size());
				logger.debug("Working busses: " + manager.getWorking().size() + " - "
						+ manager.getWorking());
			}
			if (manager.getIdle().size() > 0) {
				if (manager.getIdle().size() > 1) {
					reduce--;
					if (reduce == 0 && added > manager.getDefaultWorkerCount()) {
						EventWorker worker = (EventWorker) manager.getIdle().peek();
						if (worker != null) {
							worker.markForRemoval();
							extra--;
							textra--;
						}
					}
				}
				return;
			}
			reduce = reduceCycleCount;
			Collection working = manager.getWorking();
			Iterator i = working.iterator();
			while (i.hasNext()) {
				EventWorker worker = (EventWorker) i.next();
				if (!isBlocked(worker)) {
					return;
				}
			}
			logger.info("All busses are blocked");
			if (ThreadManager.getDefault().canAllocate(1) && false) {
				ThreadManager.getDefault().allocate(1);
				extra++;
				added++;
				textra++;
				manager.addWorker();
			}
			else {
				logger.info("Maximum number of threads reached");
			}
		}
	}

	protected boolean isBlocked(EventWorker worker) {
		return System.currentTimeMillis() - worker.getStart() > maxEventTime;
	}
}
