//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2005
 */
package org.globus.cog.karajan.workflow.service.handlers;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.rmi.server.UID;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.util.serialization.XMLConverter;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.service.InstanceContext;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.UserContext;

public class UploadHandler extends RequestHandler {
	private static final Logger logger = Logger.getLogger(UploadHandler.class);

	private InstanceContext ic;

	public void requestComplete() throws ProtocolException {
		ByteArrayInputStream bais = new ByteArrayInputStream(getInData(0));
		Inflater inflater = new Inflater();
		InputStreamReader isr = new InputStreamReader(new InflaterInputStream(bais, inflater));
		ElementTree source = XMLConverter.readSource(isr, null);

		String clientID = new String(getInData(1));
		String name = new String(getInData(2));
		UserContext uc = getChannel().getUserContext();
		ic = uc.newInstanceContext();
		ic.setClientID(clientID);
		ic.setServerID(new UID().toString());
		ic.setTree(source);
		ic.setName(name);
		addOutData(ic.getServerID());
		send();
	}

	public InstanceContext getInstanceContext() {
		return ic;
	}
}
