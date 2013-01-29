/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Jun 9, 2006
 */
package org.griphyn.vdl.karajan;

import k.rt.FutureListener;
import k.thr.LWThread;

import org.globus.cog.karajan.futures.FutureEvaluationException;
import org.globus.cog.karajan.futures.FutureIterator;
import org.globus.cog.karajan.futures.FutureIteratorIncomplete;
import org.globus.cog.karajan.futures.FutureNotYetAvailable;

public class FuturePairIterator implements FutureIterator {
	private ArrayIndexFutureList array;
	private int crt;

	public FuturePairIterator(ArrayIndexFutureList array) {
		this.array = array;
	}

	public synchronized boolean hasAvailable() {
		return crt < array.available();
	}

	public synchronized int current() {
		return crt;
	}

	public int count() {
		try {
			return array.size();
		}
		catch (FutureNotYetAvailable e) {
			throw new FutureIteratorIncomplete(array, this);
		}
	}

	public Object peek() {
		try {
			return array.get(crt);
		}
		catch (FutureNotYetAvailable e) {
			throw new FutureIteratorIncomplete(array, this);
		}
	}

	public void remove() {
		throw new UnsupportedOperationException("remove");
	}

	public synchronized boolean hasNext() {
		if (array.isClosed()) {
			return crt < array.size();
		}
		else {
			if (crt < array.available()) {
				return true;
			}
			else {
				throw new FutureIteratorIncomplete(array, this);
			}
		}
	}

	public synchronized Object next() {
		if (array.isClosed()) {
			return array.get(crt++);
		}
		else {
			if (crt < array.available()) {
				return array.get(crt++);
			}
			else {
				throw new FutureIteratorIncomplete(array, this);
			}
		}
	}

	public void close() {
		// nope
	}

	public boolean isClosed() {
		return array.isClosed();
	}

	public Object getValue() {
		return this;
	}
	
	

	@Override
    public void addListener(FutureListener l) {
		WaitingThreadsMonitor.addThread(LWThread.currentThread(), array.getHandle());
		array.addListener(l);
		
    }
	
	private static volatile int cnt = 0;

	public void fail(FutureEvaluationException e) {
		//handled by the list
	}
}
