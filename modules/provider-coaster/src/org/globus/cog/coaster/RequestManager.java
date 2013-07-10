//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2005
 */
package org.globus.cog.coaster;

import org.globus.cog.coaster.handlers.RequestHandler;

public interface RequestManager {
	RequestHandler handleInitialRequest(int tag, byte[] data)
			throws NoSuchHandlerException;
	
	void addHandler(String name, Class<? extends RequestHandler> cls);
}
