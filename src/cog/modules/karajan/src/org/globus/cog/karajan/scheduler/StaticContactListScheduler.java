
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.scheduler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.globus.cog.abstraction.interfaces.ServiceContact;


public abstract class StaticContactListScheduler extends AbstractScheduler implements Scheduler {
	private final List serviceContacts;

	public StaticContactListScheduler() {
		serviceContacts = new LinkedList();
	}

	public List getServiceContacts() {
		return new ArrayList(serviceContacts);
	}

	public void addServiceContact(ServiceContact serviceContact) {
		serviceContacts.add(serviceContact);
	}

	public void removeServiceContact(ServiceContact serviceContact) {
		serviceContacts.remove(serviceContact);
	}
}
