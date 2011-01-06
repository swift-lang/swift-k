
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
import org.globus.cog.util.ImageLoader;

public class NoAction extends FailureAction {
	
	public NoAction() {
	}
	
	public void handleFailure(EventListener element, NotificationEvent event){
		EventBus.send(element, event);
	}
	
	public String getName(){
		return "Abort";
	}
	
	public String getDescription(){
		return "The failure is passed to the workflow engine";
	}

	public Icon getIcon(){
		return ImageLoader.loadIcon("images/16x16/co/button-cancel.png");
	}
}
