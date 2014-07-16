// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 8, 2003
 */
package org.globus.cog.karajan.scheduler.submitQueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

/**
 * A class that makes <code>submit()</code> calls asynchronous. This is required
 * because, while the task submit calls are theoretically short, they may in
 * practice take arbitrarily large amounts of time, which may otherwise block
 * the scheduler thread.
 * 
 * @author Mihael Hategan
 * 
 */
public class NonBlockingSubmit implements Runnable {
	private static final Logger logger = Logger.getLogger(NonBlockingSubmit.class);
	
	private static final ExecutorService pool = Executors.newFixedThreadPool(
                        Runtime.getRuntime().availableProcessors(), new DaemonThreadFactory(
                                        Executors.defaultThreadFactory()));
	
	static class DaemonThreadFactory implements ThreadFactory {
		private ThreadFactory delegate;
        private int id;

        public DaemonThreadFactory(ThreadFactory delegate) {
                this.delegate = delegate;
        }

        public Thread newThread(Runnable r) {
                Thread t = delegate.newThread(r);
                t.setName("NBS" + (id++));
                t.setDaemon(true);
                return t;
        }
    }


	private final TaskHandler taskHandler;
	private final Task task;
	private SubmitQueue[] queues;
	private int currentQueue;
	private int attempts;
	private int id;

	private static volatile int sid = 0;

	public NonBlockingSubmit(TaskHandler th, Task task, SubmitQueue[] queues) {
		this.taskHandler = th;
		this.task = task;
		this.queues = queues;
		id = sid++;
		attempts = 0;
	}

	public void go() {
		nextQueue();
	}

	public void nextQueue() {
		if (queues != null && queues.length > currentQueue) {
			queues[currentQueue++].queue(this);
		}
		else {
			pool.submit(this);
		}
	}

	public void notifyPreviousQueue(Exception ex) {
		if (queues != null && currentQueue > 0) {
			try {
				queues[--currentQueue].submitCompleted(this, ex);
			}
			catch (Exception e) {
				logger.warn("Exception caught while notifying queue of job submission status", e);
			}
		}
		else {
			if (ex != null) {
				Status st = task.getStatus();
				if (st.getStatusCode() == Status.FAILED) {
					logger.warn("Warning: Task handler throws exception and also sets status");
				}
				else {
					Status ns = new StatusImpl();
					ns.setStatusCode(Status.FAILED);
					ns.setException(ex);
					task.setStatus(ns);
				}
			}
		}
	}

	public void run() {
		try {
			attempts++;
			taskHandler.submit(task);
			notifyPreviousQueue(null);
		}
		catch (Exception e) {
			notifyPreviousQueue(e);
		}
		catch (ThreadDeath td) {
			throw td;
		}
		catch (Throwable t) {
			notifyPreviousQueue(new Exception(t));
			t.printStackTrace();
		}
	}

	public int getAttempts() {
		return attempts;
	}

	public Task getTask() {
		return task;
	}

	public TaskHandler getTaskHandler() {
		return taskHandler;
	}
}
