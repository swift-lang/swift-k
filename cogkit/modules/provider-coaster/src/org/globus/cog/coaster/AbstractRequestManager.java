/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
