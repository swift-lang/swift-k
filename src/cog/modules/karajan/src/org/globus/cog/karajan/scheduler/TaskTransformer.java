//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 12, 2006
 */
package org.globus.cog.karajan.scheduler;

import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.util.Contact;

public interface TaskTransformer {
	public void transformTask(Task task, Contact[] contacts, Service[] services);
}
