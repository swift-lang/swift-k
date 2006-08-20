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
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.util.serialization.XMLConverter;
import org.globus.cog.karajan.workflow.events.ControlEvent;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.FlowEvent;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.service.InstanceContext;
import org.globus.cog.karajan.workflow.service.ProtocolException;

public class EventHandler extends RequestHandler {
	private final static Logger logger = Logger.getLogger(EventHandler.class);
	private InstanceContext ic;

	public void requestComplete() throws ProtocolException {
		List data = this.getInDataChuncks();
		String id = new String((byte[]) data.get(0));
		ic = getChannel().getUserContext().getInstanceContext(id);
		if (ic == null) {
			sendError("Invalid workflow id: " + id);
		}
		else {
			Integer eid = Integer.valueOf(new String((byte[]) data.get(1)));
			logger.debug("Destination UID: " + eid);
			Inflater inflater = new Inflater();
			InputStreamReader isr = new InputStreamReader(new InflaterInputStream(new ByteArrayInputStream(
					(byte[]) data.get(2)), inflater));
			try {
				Object o = XMLConverter.readObject(isr);
				if (o instanceof Event) {
					Event e = (Event) o;
					logger.debug("Event: " + e);
					FlowElement fe = ic.getTree().getUIDMap().get(eid);
					if (e instanceof ControlEvent) {
						sendError("Control events are not allowed");
					}
					ic.getUserContext().getChannelContext().initialize();
					if (e instanceof FlowEvent) {
						FlowEvent fev = (FlowEvent) e;
						if (fev.getStack() == null) {
							fev.setStack(ic.getStack().copy());
						}
					}
					EventBus.post(fe, e);
					sendReply("OK".getBytes());
				}
				else {
					sendError("Invalid event");
				}
			}
			catch (Exception e) {
				sendError("Exception caught: " + e.toString());
			}
		}
	}
}
