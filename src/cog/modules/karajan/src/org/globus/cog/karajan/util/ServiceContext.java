
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.util;

import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;


public class ServiceContext {
	private SecurityContext securityContext;
	private ServiceContact serviceContact;

	public ServiceContext(SecurityContext securityContext,
	                      ServiceContact serviceContact) {
		this.serviceContact = serviceContact;
		this.securityContext = securityContext;
	}

	public SecurityContext getSecurityContext() {
		return securityContext;
	}

	public void setSecurityContext(SecurityContext securityContext) {
		this.securityContext = securityContext;
	}

	public ServiceContact getServiceContact() {
		return serviceContact;
	}

	public void setServiceContact(ServiceContact serviceContact) {
		this.serviceContact = serviceContact;
	}

}
