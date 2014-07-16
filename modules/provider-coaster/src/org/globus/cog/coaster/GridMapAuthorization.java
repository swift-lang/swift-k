//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 19, 2005
 */
package org.globus.cog.coaster;

import java.io.IOException;

import org.globus.cog.util.GridMap;
import org.globus.gsi.gssapi.auth.Authorization;
import org.globus.gsi.gssapi.auth.AuthorizationException;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;

public class GridMapAuthorization extends Authorization {

	public GridMapAuthorization() throws IOException {
		GridMap.getGridMap();
	}

	public void authorize(GSSContext context, String host) throws AuthorizationException {
		try {
			String dn = context.getSrcName().toString();
			String name = GridMap.getGridMap().getUserID(dn);
			if (name == null) {
				throw new AuthorizationException("No local mapping for " + dn);
			}
		}
		catch (IOException e) {
			throw new AuthorizationException(e.getMessage(), e);
		}
		catch (GSSException e) {
			throw new AuthorizationException(e.getMessage(), e);
		}
	}
}
