//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 13, 2012
 */
package k.thr;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import k.rt.ConditionalYield;
import k.rt.ExecutionException;
import k.rt.Future;
import k.rt.FutureListener;


public class ThreadSetFixed implements Future {
	private LWThread[] threads;
	private FutureListener listener;
	private ExecutionException ex;
	private boolean abort;
	private int running;
	
	public ThreadSetFixed(int size) {
		threads = new LWThread[size];
	}
	
	public synchronized boolean add(LWThread thread) {
		if (!abort) {
		    running++;
		    threads[thread.getForkID()] = thread;
		}
		return abort;
	}
	
	public synchronized void lock() {
		running++;
	}
	
	public synchronized void unlock() {
		running--;
		if (running == 0) {
		    notifyListeners();
		}
	}
	
	public synchronized void startAll() {
		for (LWThread t : threads) {
			t.start();
		}
	}
	
	public synchronized int getRunning() {
	    return running;
	}
	
	public synchronized void abortAll() {
	    if (threads == null) {
	        return;
	    }
		abort = true;
		for (LWThread thread : threads) {
			if (thread != null) {
				thread.abort();
			}
		}
		threads = null;
		notifyListeners();
	}

	public synchronized void threadDone(LWThread thr, ExecutionException e) {
		if (abort) {
			return;
		}
		if (this.ex == null) {
			this.ex = e;
		}
		threads[thr.getForkID()] = null;
		running--;
		if (running == 0) {
			threads = null;
			notifyListeners();
		}
	}

	private void notifyListeners() {
		if (listener != null) {
			listener.futureUpdated(this);
			listener = null;
		}
	}

	@Override
	public synchronized void addListener(FutureListener l, ConditionalYield y) {
		if (listener != null) {
			throw new IllegalThreadStateException("Multiple listeners");
		}
		listener = l;
		if (running == 0 || ex != null || abort) {
			notifyListeners();
		}
	}
	
	public synchronized boolean allDone() {
		return running == 0;
	}
	
	public synchronized Exception getException() {
		return ex;
	}

	public synchronized void waitFor() {
		if (threads == null) {
			if (ex == null) {
				return;
			}
			else {
				throw ex;
			}
		}
		else {
			throw new ConditionalYield(this);
		}
	}

	
	private static final NumberFormat NF = new DecimalFormat("00000000");
	private volatile String ts; 
	@Override
	public String toString() {
		if (ts == null) {
			ts = "TS[" + NF.format(System.identityHashCode(this)) + "]";
		}
		return ts;
	}

	public synchronized boolean anyDone() {
		return running == 0 || running < threads.length;
	}

	public synchronized void checkFailed() {
		if (ex != null) {
			throw ex;
		}
	}
}
