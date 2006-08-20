//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2005
 */
package org.globus.cog.karajan.workflow.service;

import org.globus.cog.karajan.workflow.service.handlers.EchoHandler;
import org.globus.cog.karajan.workflow.service.handlers.EventHandler;
import org.globus.cog.karajan.workflow.service.handlers.VargHandler;
import org.globus.cog.karajan.workflow.service.handlers.VersionHandler;

public class ClientRequestManager extends AbstractRequestManager {
	public ClientRequestManager() {
		addHandler("VERSION", VersionHandler.class);
		addHandler("EVENT", EventHandler.class);
		addHandler("VARG", VargHandler.class);
		addHandler("ECHO", EchoHandler.class);
		//addHandler("TEST", TestHandler.class);
	}
}
