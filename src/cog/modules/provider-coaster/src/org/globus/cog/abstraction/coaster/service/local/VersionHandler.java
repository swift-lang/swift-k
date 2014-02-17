//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2005
 */
package org.globus.cog.abstraction.coaster.service.local;

import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.handlers.RequestHandler;


public class VersionHandler extends RequestHandler {
    public static final String NAME = "VERSION";
	public static final String VERSION = "0.1";
	
	public void requestComplete() throws ProtocolException {
		sendReply(VERSION.getBytes());
	}
}
