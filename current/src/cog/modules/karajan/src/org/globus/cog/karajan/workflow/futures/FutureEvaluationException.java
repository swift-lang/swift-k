//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 6, 2006
 */
package org.globus.cog.karajan.workflow.futures;

import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;


public class FutureEvaluationException extends KarajanRuntimeException {
	private Throwable fault;

	public FutureEvaluationException(FailureNotificationEvent fne) {
		this.fault = new ExecutionException(fne.getException());
	}
	
	public FutureEvaluationException(Throwable fault) {
		this.fault = fault;
	}

	public Throwable getFault() {
		return fault;
	}
}
