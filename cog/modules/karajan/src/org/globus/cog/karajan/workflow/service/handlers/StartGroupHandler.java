//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 9, 2005
 */
package org.globus.cog.karajan.workflow.service.handlers;

import org.globus.cog.karajan.workflow.service.ProtocolException;

public class StartGroupHandler extends GroupHandler {
	private String instanceID;

	public void handlerRequestComplete(RequestHandler handler) throws ProtocolException {
		if (handler instanceof UploadHandler) {
			UploadHandler uh = (UploadHandler) handler;
			uh.receiveCompleted();
			instanceID = uh.getInstanceContext().getID();
		}
		else if (handler instanceof StartHandler) {
			handler.setInData(0, instanceID.getBytes());
			handler.receiveCompleted();
			sendReply("OK".getBytes());
		}
		else {
			handler.receiveCompleted();
		}
	}
}
