//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 19, 2005
 */
package org.globus.cog.coaster;

import org.globus.gsi.gssapi.auth.Authorization;
import org.globus.gsi.gssapi.auth.AuthorizationException;
import org.globus.gsi.gssapi.auth.GSSAuthorization;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;

public class FallbackAuthorization extends GSSAuthorization {
	private final Authorization[] authz;

	public FallbackAuthorization(Authorization[] authz) {
		this.authz = authz;
	}

	public GSSName getExpectedName(GSSCredential cred, String host) throws GSSException {
		return cred.getName();
	}

	public void authorize(GSSContext context, String host) throws AuthorizationException {
		if (authz == null || authz.length == 0) {
			throw new AuthorizationException("No authorization");
		}
		else {
			String message = "";
			for (int i = 0; i < authz.length; i++) {
				try {
					authz[i].authorize(context, host);
					return;
				}
				catch (AuthorizationException e) {
					message = message + "\n" + e.getMessage();
				}
			}
			throw new AuthorizationException(message);
		}
	}
}
