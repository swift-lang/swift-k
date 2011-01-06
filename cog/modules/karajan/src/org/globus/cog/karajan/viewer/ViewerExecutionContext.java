//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 9, 2005
 */
package org.globus.cog.karajan.viewer;

import org.globus.cog.karajan.util.KarajanProperties;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEvent;

public class ViewerExecutionContext extends ExecutionContext {

	private KarajanFrame frame;
	
	public ViewerExecutionContext(ElementTree tree, KarajanFrame frame) {
		super(tree);
		this.frame = frame;
	}

	public ViewerExecutionContext(ElementTree tree, KarajanProperties properties) {
		super(tree, properties);
	}

	protected void completed(NotificationEvent e) {
		frame.completed();
	}

	protected void failed(NotificationEvent e) {
		frame.failed((FailureNotificationEvent) e);
	}
}
