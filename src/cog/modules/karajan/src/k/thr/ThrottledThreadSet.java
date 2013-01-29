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


public class ThrottledThreadSet extends ThreadSet {
    private final int maxThreads;
    private final FreeSlotFuture fsf;
    
    private class FreeSlotFuture extends AbstractFuture {
		@Override
		protected boolean isClosed() {
			return false;
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
	    return maxThreads - getRunning();
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
