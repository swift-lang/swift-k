/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
