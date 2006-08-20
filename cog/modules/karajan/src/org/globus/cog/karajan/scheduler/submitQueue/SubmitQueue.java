//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 21, 2006
 */
package org.globus.cog.karajan.scheduler.submitQueue;

public interface SubmitQueue {
	void queue(NonBlockingSubmit nbs);
    
    void submitCompleted(NonBlockingSubmit nbs, Exception ex);
}
