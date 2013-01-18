//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 7, 2008
 */
package org.globus.cog.karajan.scheduler.submitQueue;

import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

public class NonBlockingCancel extends NonBlockingSubmit {
	private String message;

	public NonBlockingCancel(TaskHandler th, Task task, String message) {
		super(th, task, null);
		this.message = message;
	}
	
	public void run() {
		try {
			getTaskHandler().cancel(getTask(), message);
			notifyPreviousQueue(null);
		}
		catch (Exception e) {
			//force it
			getTask().setStatus(Status.CANCELED);
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
}
