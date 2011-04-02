
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 10, 2003
 */
package org.globus.cog.karajan.viewer;


import javax.swing.Icon;

import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;
import org.globus.cog.util.ImageLoader;

public class IgnoreErrors extends FailureAction {

	public void handleFailure(EventListener element,
		NotificationEvent event) {
		event.setType(NotificationEventType.EXECUTION_COMPLETED);
		EventBus.send(element, event);
	}

	public String getName() {
		return "Ignore";
	}

	public String getDescription() {
		return "Ignores the error and continues";
	}

	public Icon getIcon() {
		return ImageLoader.loadIcon("images/16x16/co/attention.png");
	}
}
