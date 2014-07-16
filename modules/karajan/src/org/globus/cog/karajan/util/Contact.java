//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 2, 2004
 */
package org.globus.cog.karajan.util;

/**
 * This class implements a token representing a contact
 * (i.e. remote host). However, it provides no concrete
 * information about such a host. It is mostly used as
 * a "virtual" contact that is to be bound later to a 
 * concrete site.
 * 
 */
import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.karajan.scheduler.TaskConstraints;

public abstract class Contact {
	private Identity identity;
	private TaskConstraints constraints;
	
	public Contact() {
	    identity = new IdentityImpl("host");
	}
	
	public boolean equals(Object obj) {
	    if (this == obj) {
	        return true;
	    }
		if (obj instanceof Contact){
			return identity.equals(((Contact) obj).identity);
		}
		return false;
	}
	
	public int hashCode() {
		return identity.hashCode();
	}

	public Identity getId() {
		return identity;
	}
	
	public abstract boolean isVirtual();

	public TaskConstraints getConstraints() {
		return constraints;
	}

	public void setConstraints(TaskConstraints constraints) {
		this.constraints = constraints;
	}
}
