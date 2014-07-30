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
 * Created on Dec 1, 2012
 */
package k.thr;

import k.rt.AbstractFuture;
import k.rt.ConditionalYield;
import k.rt.ExecutionException;
import k.rt.FutureListener;


public class ThrottledThreadSet extends ThreadSet {
    private final int maxThreads;
    private final FreeSlotFuture fsf;
    
    private class FreeSlotFuture extends AbstractFuture {
		@Override
		protected boolean isClosed() {
			return false;
		}

		@Override
		public void addListener(FutureListener l, ConditionalYield y) {
		    synchronized(ThrottledThreadSet.this) {
    		    if (canAdd()) {
    		        l.futureUpdated(this);
    		    }
    		    else {
    		    	super.addListener(l, y);
    		    }
		    }
		}

		@Override
		protected void notifyListeners() {
			super.notifyListeners();
		}
    }
    
	public ThrottledThreadSet(int maxThreads) {
        super();
        this.maxThreads = maxThreads;
        fsf = new FreeSlotFuture();
    }
	
	public boolean canAdd() {
	    return freeSlots() > 0;
	}
	
	public int freeSlots() {
	    // this is not exactly correct, but if the set is locked
	    // getRunning() is artificially increased by one. The assumption
	    // here is that this method is only called with the set locked.
	    return maxThreads - getRunning() + 1;
	}
	
	@Override
	public synchronized void threadDone(LWThread thr, ExecutionException e) {
		super.threadDone(thr, e);
		fsf.notifyListeners();
	}

	public void waitForSlot() {
	    if (!canAdd()) {
	        throw new ConditionalYield(fsf);
	    }
	}
}
