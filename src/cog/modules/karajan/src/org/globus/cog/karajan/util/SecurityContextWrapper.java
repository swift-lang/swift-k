
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Feb 6, 2004
 *
 */
package org.globus.cog.karajan.util;

import java.util.HashMap;
import java.util.Iterator;

import org.globus.cog.abstraction.interfaces.SecurityContext;

public class SecurityContextWrapper {
	//TODO remove this class
	private Object credentials;
	private HashMap attributes;
	private SecurityContext securityContext;

	public SecurityContextWrapper() {
		attributes = new HashMap();
	}
	
	public void setCredentials(Object credentials) {
		this.credentials = credentials;
		if (securityContext != null) {
			securityContext.setCredentials(credentials);
		}
	}

	public Object getCredentials() {
		return credentials;
	}

	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
		if (securityContext != null) {
			securityContext.setAttribute(name, value);
		}
	}

	public Object getAttribute(String name) {
		return attributes.get(name);
	}
	
	public HashMap getAttributes() {
		return attributes;
	}
	
	public void setAttributes(HashMap attributes) {
		this.attributes = attributes;
	}
	
	public SecurityContext getSecurityContext() {
		return this.securityContext;
	}

	public void setSecurityContext(SecurityContext securityContext) {
		this.securityContext = securityContext;
		securityContext.setCredentials(credentials);
		Iterator i = attributes.keySet().iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			securityContext.setAttribute(key, attributes.get(key));
		}
	}

}
