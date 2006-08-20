//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 29, 2006
 */
package org.globus.cog.karajan.workflow.service.handlers;

import java.io.File;
import java.rmi.server.UID;

import org.globus.cog.karajan.Loader;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.service.InstanceContext;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.UserContext;

public class SubmitHandler extends RequestHandler {
	private String file, clientID, user;

	public void requestComplete() throws ProtocolException {
		if (!getChannel().getChannelContext().getServiceContext().isLocal()) {
			throw new ProtocolException("Submission is only available on a local service");
		}
		user = new String(getInData(0));
		clientID = new String(getInData(1));
		file = new String(getInData(2));
		File f = new File(file);
		if (f.canRead()) {
			try {
				ElementTree tree = Loader.load(f.getAbsolutePath());
				UserContext uc = getChannel().getUserContext().getChannelContext().newUserContext(user);
				InstanceContext ic = uc.newInstanceContext();
				ic.setClientID(clientID);
				ic.setServerID(new UID().toString());
				ic.setTree(tree);
				ic.setName(f.getName());
				addOutData(ic.getServerID());
				ExecutionContext ec = new ExecutionContext(tree);
				ic.registerExecutionContext(ec);
				ec.start();
				sendReply("OK".getBytes());
			}
			catch (Exception e) {
				throw new ProtocolException(e);
			}
		}
	}
}
