//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 24, 2005
 */
package org.globus.cog.coaster;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.handlers.RequestHandler;
import org.globus.cog.coaster.handlers.UnknownCommandHandler;

public abstract class AbstractRequestManager implements RequestManager {
    public static final Logger logger = Logger.getLogger(AbstractRequestManager.class);
    
	private final Map<String, Class<? extends RequestHandler>> handlers;

	public AbstractRequestManager() {
		handlers = new HashMap<String, Class<? extends RequestHandler>>();
	}

	public void addHandler(String cmd, Class<? extends RequestHandler> cls) {
		handlers.put(cmd, cls);
	}

	public RequestHandler handleInitialRequest(int tag, byte[] data) throws NoSuchHandlerException {
		String cmd = new String(data).toUpperCase();
		Class<? extends RequestHandler> handlerClass = handlers.get(cmd);
		RequestHandler handler;
		if (handlerClass == null) {
			logger.warn(getClass().getSimpleName() 
					+ ": unknown handler(tag: " + tag + ", cmd: " 
					+ truncate(cmd) + "). Available handlers: " + handlers);
			handler = new UnknownCommandHandler();
		}
		else {
			try {
				handler = handlerClass.newInstance();
			}
			catch (Exception e) {
				return new UnknownHandler(cmd);
			}
		}
		handler.setInCmd(cmd);
		return handler;
	}
	
	public static final int MAX_CMD_LEN = 32;
	
	private String truncate(String cmd) {
		if (cmd.length() < MAX_CMD_LEN) {
			return cmd;
		}
		else {
			return cmd.substring(0, MAX_CMD_LEN) + "...";
		}
	}

	public String toString() {
		return handlers.toString();
	}
	
	public static class UnknownHandler extends RequestHandler {
		private String type;
		
		public UnknownHandler(String type) {
			this.type = type;
		}
		
		@Override
		public void requestComplete() throws ProtocolException {
			sendError("No such handler: " + type);
		}
	}
}
