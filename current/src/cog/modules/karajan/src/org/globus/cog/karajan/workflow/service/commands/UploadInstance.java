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
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.util.serialization.XMLConverter;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.service.InstanceContext;
import org.globus.cog.karajan.workflow.service.ProtocolException;

public class UploadInstance extends Command {
	private static final Logger logger = Logger.getLogger(UploadInstance.class);

	private final ElementTree tree;
	private final InstanceContext ic;
	private final String name;

	public UploadInstance(ElementTree tree, InstanceContext ic, String name) {
		super("UPLOAD");
		this.tree = tree.copy();
		this.ic = ic;
		this.name = name;
	}

	public void send() throws ProtocolException {
		serialize();
		super.send();
	}

	private void serialize() throws ProtocolException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Deflater deflater = new Deflater(Deflater.BEST_SPEED);
		OutputStreamWriter osw = new OutputStreamWriter(new DeflaterOutputStream(baos, deflater));
		try {
			XMLConverter.serializeTree(tree, osw);
			osw.close();
			baos.close();
		}
		catch (IOException e) {
			throw new ProtocolException("Could not serialize instance", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info("len = " + deflater.getTotalIn() + ", compressed = "
					+ deflater.getTotalOut());
		}
		if (logger.isDebugEnabled()) {
			try {
				FileWriter fos = new FileWriter("eworkflow.xml");
				XMLConverter.serializeTree(tree, fos);
				fos.close();
			}
			catch (Exception e) {
			}
		}

		addOutData(baos.toByteArray());
		addOutData(ic.getClientID());
		if (name != null) {
			addOutData(name);
		}
		else {
			addOutData("-");
		}
	}

	public void receiveCompleted() {
		ic.setServerID(new String(getInData()));
		// we have a complete ID for the workflow now, so it can be registered
		ic.getUserContext().registerInstanceContext(ic);
		super.receiveCompleted();
	}

	public InstanceContext getInstanceContext() {
		return ic;
	}
}
