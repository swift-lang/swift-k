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
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.BoundContact;

public class ContactAllocationTask extends TaskImpl {
	private BoundContact contact;
	private VariableStack stack;
	
	public ContactAllocationTask() {
		setName("Contact allocation task");
	}

	public BoundContact getContact() {
		return contact;
	}

	public void setContact(BoundContact contact) {
		this.contact = contact;
	}

	public VariableStack getStack() {
		return stack;
	}

	public void setStack(VariableStack stack) {
		this.stack = stack;
	}

	public int getRequiredServices() {
		return 1;
	}
}
