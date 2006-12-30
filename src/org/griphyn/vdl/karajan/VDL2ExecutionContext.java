/*
 * Created on Dec 23, 2006
 */
package org.griphyn.vdl.karajan;

import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;

public class VDL2ExecutionContext extends ExecutionContext {

	public VDL2ExecutionContext(ElementTree tree) {
		super(tree);
	}

	protected void printFailure(FailureNotificationEvent e) {
		String msg = e.getMessage();
		if (!"Execution completed with errors".equals(msg)) {
			if (msg == null && e.getException() != null) {
				msg = getMeaningfulMessage(e.getException());
			}
			getStderr().append("Execution failed:\n\t");
			String translation = VDL2ErrorTranslator.getDefault().translate(msg);
			if (translation != null) {
				getStderr().append(translation);
			}
			else {
				getStderr().append(e.toString());
			}
			getStderr().append("\n");
		}
		else {
			//lazy errors are on and they have already been printed
		}
	}
}
