//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 3, 2006
 */
package org.globus.cog.karajan.scheduler;

import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Contact;

public class ContactAllocationTask extends TaskImpl {
	private BoundContact contact;
	private Contact virtualContact;
	
	public ContactAllocationTask() {
		setName("Contact allocation task");
	}

	@Override
	protected Identity newIdentity() {
		return null;
	}

	public BoundContact getContact() {
		return contact;
	}

	public void setContact(BoundContact contact) {
		this.contact = contact;
	}

	public int getRequiredServices() {
		return 1;
	}

	public void setVirtualContact(Contact vc) {
		this.virtualContact = vc;
	}
	
	public Contact getVirtualContact() {
		return this.virtualContact;
	}
}
