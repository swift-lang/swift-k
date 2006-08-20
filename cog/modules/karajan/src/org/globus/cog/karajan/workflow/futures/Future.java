//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 15, 2005
 */
package org.globus.cog.karajan.workflow.futures;

import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventListener;

public interface Future {

	void close();
	
	boolean isClosed();
	
	Object getValue() throws VariableNotFoundException;

	/** 
	 * When the future becomes available, send the event to the element.
	 */
	void addModificationAction(EventListener target, Event event);
}
