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
import java.util.List;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.util.serialization.XMLConverter;
import org.globus.cog.karajan.workflow.service.InstanceContext;
import org.globus.cog.karajan.workflow.service.ProtocolException;

public class VargHandler extends RequestHandler {
	private String id, name;
	private Object data;

	public void requestComplete() throws ProtocolException {
		List data = getInDataChuncks();
		id = new String((byte[]) data.get(0));
		name = new String((byte[]) data.get(1));
		InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(
					(byte[]) data.get(2)));
		Object o = XMLConverter.readObject(isr);
		InstanceContext ic = getChannel().getUserContext().getInstanceContext(id);
		try {
			Arg.Channel channel = Arg.Channel.getInstance(name);
			channel.ret(ic.getStack(), o);
			sendReply("OK".getBytes());
		}
		catch (VariableNotFoundException e) {
			e.printStackTrace();
			sendError(e.toString());
		}
	}
}
