//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 24, 2005
 */
package org.globus.cog.karajan.workflow.service;

import java.util.Hashtable;
import java.util.Map;

import org.globus.cog.karajan.workflow.service.handlers.RequestHandler;
import org.globus.cog.karajan.workflow.service.handlers.UnknownCommandHandler;

public abstract class AbstractRequestManager implements RequestManager {
	private final Map handlers;

	public AbstractRequestManager() {
		handlers = new Hashtable();
	}

	public void addHandler(String cmd, Class cls) {
		handlers.put(cmd, cls);
	}

	public RequestHandler handleInitialRequest(byte[] data) throws NoSuchHandlerException {
		String cmd = new String(data).toUpperCase();
		Class handlerClass = (Class) handlers.get(cmd);
		RequestHandler handler;
		if (handlerClass == null) {
			handler = new UnknownCommandHandler();
		}
		else {
			try {
				handler = (RequestHandler) handlerClass.newInstance();
			}
			catch (Exception e) {
				throw new NoSuchHandlerException("Could not instantiate handler for " + cmd, e);
			}
		}
		handler.setInCmd(cmd);
		return handler;
	}
}
