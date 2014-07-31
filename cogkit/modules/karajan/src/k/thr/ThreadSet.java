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
import java.util.HashSet;
import java.util.Set;

import k.rt.ConditionalYield;
import k.rt.ExecutionException;
import k.rt.Future;
import k.rt.FutureListener;


public class ThreadSet implements Future {
	private Set<LWThread> threads;
	private FutureListener listener;
	private ExecutionException ex;
	private boolean abort, anyDone;
	
	public ThreadSet() {
		threads = new HashSet<LWThread>();
	}
	
	public synchronized boolean add(LWThread thread) {
		if (!abort) {
			if (threads == null) {
				threads = new HashSet<LWThread>();
			}
			threads.add(thread);
		}
		return abort;
	}
	
	public synchronized void lock() {
		add(null);
	}
	
	public synchronized void unlock() {
		threadDone(null, null);
	}
	
	public synchronized void startAll() {
		for (LWThread t : threads) {
			t.start();
		}
	}
	
	public synchronized int getRunning() {
	    if (threads == null) {
	        return 0;
	    }
	    else {
	    	return threads.size();
	    }
	}
	
	public synchronized void abortAll() {
		abort = true;
		if (threads == null) {
			return;
		}
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
		anyDone = true;
		if (this.ex == null) {
			this.ex = e;
		}
		threads.remove(thr);
		if (threads.isEmpty()) {
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
		if (threads == null || ex != null || abort) {
			notifyListeners();
		}
	}
	
	public synchronized boolean allDone() {
		return threads == null;
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
		return anyDone;
	}

	public synchronized void checkFailed() {
		if (ex != null) {
			throw ex;
		}
	}
}
