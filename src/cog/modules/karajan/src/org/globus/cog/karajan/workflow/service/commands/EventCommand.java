//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2005
 */
package org.globus.cog.karajan.workflow.service.commands;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.util.serialization.XMLConverter;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.service.InstanceContext;
import org.globus.cog.karajan.workflow.service.ProtocolException;

public class EventCommand extends Command {
	private static final Logger logger = Logger.getLogger(EventCommand.class);

	private final InstanceContext workflow;
	private final String destUID;
	private Event e;

	public EventCommand(InstanceContext workflow, FlowElement dest, Event e) {
		super("EVENT");
		this.workflow = workflow;
		this.destUID = dest.getProperty(FlowElement.UID).toString();
		this.e = e;
	}
	
	public EventCommand(InstanceContext workflow, int destUID, Event e) {
		super("EVENT");
		this.workflow = workflow;
		this.destUID = String.valueOf(destUID);
		this.e = e;
	}

	public void send() throws ProtocolException {
		serialize();
		super.send();
	}

	private void serialize() throws ProtocolException {
		addOutData(workflow.getID().getBytes());
		addOutData(destUID);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// XML data, with low entropy, so the difference between
		// high compression and low compression is little
		// Wait, does that make any sense?
		Deflater deflater = new Deflater(Deflater.BEST_SPEED);
		OutputStreamWriter osw = new OutputStreamWriter(new DeflaterOutputStream(baos, deflater));
		try {
			if (e instanceof FailureNotificationEvent) {
				FailureNotificationEvent ne = (FailureNotificationEvent) e;
				FailureNotificationEvent copy = new FailureNotificationEvent(ne.getFlowElement(),
						null, ne.toString(), ne.getException());
				e = copy;
			}
			XMLConverter.serializeEvent(e, workflow.getTree(), osw);
			osw.close();
			baos.close();
		}
		catch (IOException e) {
			throw new ProtocolException("Could not serialize event", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info("len = " + deflater.getTotalIn() + ", compressed = "
					+ deflater.getTotalOut());
		}
		addOutData(baos.toByteArray());

		if (logger.isDebugEnabled()) {
			try {
				FileOutputStream fos = new FileOutputStream("event" + this.getId() + ".xml");
				XMLConverter.serializeEvent(e, workflow.getTree(), new OutputStreamWriter(fos));
				fos.close();
			}
			catch (Exception e) {

			}
		}
	}

	public String getWorkflowId() {
		return new String(getInData());
	}
}
