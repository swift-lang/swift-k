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
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.globus.cog.karajan.util.serialization.XMLConverter;
import org.globus.cog.karajan.workflow.service.InstanceContext;
import org.globus.cog.karajan.workflow.service.ProtocolException;

public class VargCommand extends Command {
	private final Object value;
	private final String name;
	private final InstanceContext ic;

	public VargCommand(InstanceContext ic, String name, Object value) {
		super("VARG");
		this.ic = ic;
		this.name = name;
		this.value = value;
	}

	public void send() throws ProtocolException {
		serialize();
		super.send();
	}

	private void serialize() throws ProtocolException {
		addOutData(ic.getID().getBytes());
		addOutData(name.getBytes());
		XMLConverter converter = new XMLConverter();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(baos);
		converter.write(value, osw);
		try {
			osw.close();
			baos.close();
		}
		catch (IOException e) {
			throw new Error("JVM error #0134a");
		}
		addOutData(baos.toByteArray());
	}
}
