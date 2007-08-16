//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2006
 */
package org.globus.cog.karajan.scheduler;

import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.karajan.util.BoundContact;


public class ThrottlingScheduler extends LateBindingScheduler {
    
	protected BoundContact getNextContact(TaskConstraints constraints) throws NoFreeResourceException {
		throw new UnsupportedOperationException("No host(s) specified");
	}
	
	public Service resolveService(BoundContact contact, int taskType) {
		TaskConstraints tc = contact.getConstraints();
		if (tc != null) {
			String provider = (String) tc.getConstraint("provider");
			return contact.getService(taskType, provider);
		}
		else {
			return contact.getService(taskType, null);
		}
	}
}
