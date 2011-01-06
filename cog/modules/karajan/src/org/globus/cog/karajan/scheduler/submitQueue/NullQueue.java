//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2006
 */
package org.globus.cog.karajan.scheduler.submitQueue;

/**
 * A queue that simply forwards to the next queue.
 * 
 * @author Mihael Hategan
 *
 */
public class NullQueue implements SubmitQueue {
	public void queue(NonBlockingSubmit nbs) {
		nbs.nextQueue();
	}

	public void submitCompleted(NonBlockingSubmit nbs, Exception ex) {
		nbs.notifyPreviousQueue(ex);
	}
}
