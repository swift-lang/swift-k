//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.slocal;

import java.util.Collection;

import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Task;
import org.ietf.jgss.GSSCredential;

public class Util {
	public static void checkCredentials(Task task) throws InvalidSecurityContextException {
		Collection c = task.getAllServices();
		if (c == null || c.isEmpty()) {
			throw new InvalidSecurityContextException("No security context specified");
		}
		Service service = (Service) c.iterator().next();
		if (service.getSecurityContext() == null) {
			throw new InvalidSecurityContextException("No credentials supplied");
		}
		if (!(service.getSecurityContext().getCredentials() instanceof GSSCredential)) {
			throw new InvalidSecurityContextException("Invalid credentials");
		}
	}
}
