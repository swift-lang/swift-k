//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 6, 2006
 */
package org.globus.cog.karajan.workflow.futures;

import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;


public class FutureEvaluationException extends KarajanRuntimeException {
	private FailureNotificationEvent failure;

	public FutureEvaluationException(FailureNotificationEvent failure) {
		this.failure = failure;
	}

	public FailureNotificationEvent getFailure() {
		return failure;
	}
}
