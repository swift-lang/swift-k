//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 29, 2007
 */
package k.thr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import k.rt.ExecutionException;
import k.rt.KRunnable;
import k.rt.Stack;


public final class Scheduler {
	private static final Scheduler scheduler = new Scheduler();

	public static Scheduler getScheduler() {
		return scheduler;
	}
	
	private ThreadPoolExecutor workers;
	private BlockingQueue<Runnable> queue;
	private final Set<LWThread> sleeping;
	private int crt;
	private final Timer timer;
	
	private Scheduler() {
		sleeping = new HashSet<LWThread>();
		timer = new Timer("LWT Scheduler Timer");
		queue = new LinkedBlockingQueue<Runnable>();
		int ps = Runtime.getRuntime().availableProcessors() * 2;
		workers = new ThreadPoolExecutor(ps, ps, 0, TimeUnit.MILLISECONDS, queue);
		Clock.init();
	}

	public void awake(final LWThread thread) {
		boolean wasSleeping;
		synchronized(sleeping) {
			wasSleeping = sleeping.remove(thread);
		}
		if (wasSleeping) {
			//System.err.println("awake(" + thread + ")");
			schedule(thread);
		}
	}
	
	public void awakeAfter(final LWThread thread, long delay) {
		if (delay == 0) {
			new Throwable().printStackTrace();
		}
		//System.err.println("awakeAfter(" + thread + ", " + delay + ")");
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				awake(thread);
			}}, delay);
	}
	
	/**
	 * Returns a number that can be used to determine if any progress has
	 * been made since the last call to this method. If any progress has
	 * been made (i.e. threads have successfully completed at least one
	 * time slice), then the number returned by this method is guaranteed
	 * to be different from the last call.
	 */
	public long getSequenceId() {
		return workers.getCompletedTaskCount();
	}
	
	protected void schedule(final LWThread thread) {
	    workers.execute(thread);
	}
	
	public synchronized boolean isAnythingRunning() {
		return workers.getActiveCount() > 0 || !queue.isEmpty();
	}
	
	public void putToSleep(final LWThread thread) {
		if (thread.isSleeping()) {
			//System.err.println("putToSleep(" + thread + ")");
			synchronized(sleeping) {
				sleeping.add(thread);
			}
		}
	}
	
	protected Timer getTimer() {
		return timer;
	}
		
	public static class RootThread extends LWThread {
		private RuntimeException e;
		private boolean printError;
		
		public RootThread(KRunnable r, Stack stack) {
			this(r, stack, false);
		}
		
		public RootThread(KRunnable r, Stack stack, boolean printError) {
			super("R", r, stack);
			this.printError = printError;
		}

		@Override
		protected void done(RuntimeException e) {
			this.e = e;
			setState(ALIVE, false);
			if (e != null && printError) {
				System.err.println(this);
				if (e instanceof ExecutionException) {
					ExecutionException ee = (ExecutionException) e;
					if (ee.getStack() == null) {
						ee.setStack(getStack());
					}
					System.err.println(e.toString());
					e.printStackTrace();
				}
				else {
					e.printStackTrace();
				}
			}
			synchronized (this) {
				notifyAll();
			}
		}

		@Override
		public RootThread getRoot() {
			return this;
		}
		
		@Override
		public synchronized void waitFor() {
			super.waitFor();
			if (e != null) {
				throw e;
			}
		}
	}

	public List<LWThread> getSleepingThreads() {
		synchronized(sleeping) {
			return new ArrayList<LWThread>(sleeping);
		}
	}
}
