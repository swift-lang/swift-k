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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.StackFrame;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.DefList;
import org.globus.cog.karajan.util.DefUtil;
import org.globus.cog.karajan.util.DefinitionEnvironment;
import org.globus.cog.karajan.util.serialization.XMLConverter;
import org.globus.cog.karajan.workflow.JavaElement;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.user.UDEDefinition;
import org.globus.cog.karajan.workflow.service.InstanceContext;
import org.globus.cog.karajan.workflow.service.ProtocolException;

public class StartCommand extends Command {
	private static final Logger logger = Logger.getLogger(StartCommand.class);

	private final InstanceContext workflow;
	private final FlowElement dest;
	private final VariableStack stack;

	public StartCommand(InstanceContext workflow, FlowElement dest, VariableStack stack) {
		super("START");
		this.workflow = workflow;
		this.dest = dest;
		this.stack = prepareStack(stack);
	}

	public void send() throws ProtocolException {
		serialize();
		super.send();
	}

	private void serialize() throws ProtocolException {
		addOutData(workflow.getID().getBytes());
		addOutData(dest.getProperty(FlowElement.UID).toString());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// XML data, with low entropy, so the difference between
		// high compression and low compression is little
		Deflater deflater = new Deflater(Deflater.BEST_SPEED);
		OutputStreamWriter osw = new OutputStreamWriter(new DeflaterOutputStream(baos, deflater));
		try {
			XMLConverter converter = XMLConverter.createStateMarshallingConverter(stack.getExecutionContext().getTree());
			converter.getKContext().setElementMarshallingPolicy(
					new RemoteElementMarshallingPolicy());
			converter.registerConverter(
						new RUDEDefinitionConverter(converter.getKContext(),
								new DefinitionEnvironment(stack)), 1);
			converter.write(stack, osw);
			osw.close();
			baos.close();
		}
		catch (IOException e) {
			throw new ProtocolException("Could not serialize stack", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info("len = " + deflater.getTotalIn() + ", compressed = "
					+ deflater.getTotalOut());
		}
		addOutData(baos.toByteArray());

		if (logger.isDebugEnabled()) {
			try {
				FileOutputStream fos = new FileOutputStream("sestack" + this.getId() + ".xml");
				XMLConverter converter = XMLConverter.createStateMarshallingConverter(stack.getExecutionContext().getTree());
				converter.getKContext().setElementMarshallingPolicy(
						new RemoteElementMarshallingPolicy());
				converter.registerConverter(
						new RUDEDefinitionConverter(converter.getKContext(),
								new DefinitionEnvironment(stack)), 1);
				converter.write(stack, new OutputStreamWriter(fos));
				fos.close();
			}
			catch (Exception e) {

			}
		}
	}

	public String getWorkflowId() {
		return new String(getInData());
	}

	protected final VariableStack prepareStack(VariableStack stack) {
		Set names = new HashSet();
		Set imports;
		try {
			imports = (Set) stack.getVar("#imports");
		}
		catch (VariableNotFoundException e) {
			imports = Collections.EMPTY_SET;
		}
		VariableStack copy = stack.newInstance();
		names.addAll(stack.firstFrame().names());
		for (int i = 1; i < stack.frameCount(); i++) {
			StackFrame frame = stack.getFrame(i);
			names.addAll(frame.names());
		}
		copy.enter();
		Iterator ni = names.iterator();
		while (ni.hasNext()) {
			String name = (String) ni.next();
			try {
				if (name.startsWith("##")) {
					continue;
				}
				else if (name.startsWith("#")) {
					if (name.equals("#vargs") || name.equals("#nargs")) {
						copy.setVar(name, true);
					}
					else if (name.startsWith("#chanbuf")) {
						continue;
					}
					else if (name.startsWith("#channel#")) {
						// handled on the other side
						continue;
					}
					else if (name.equals("#env")) {
						// too complex to handle for now
						continue;
					}
                    else if (name.equals("#argthread")) {
                        // there should be a more sane way to do these checks
                        continue;
                    }
					else if (name.equals("#caller")) {
						copy.setVar("#calleruid",
								((FlowElement) stack.getVar("#caller")).getProperty(FlowElement.UID));
					}
					else if (name.startsWith("#def#")) {
						DefList def = (DefList) stack.getVar(name);
						Iterator d = def.prefixes().iterator();
						while (d.hasNext()) {
							String prefix = (String) d.next();
							Object value = def.get(prefix);
							// TODO This check is only made for performance
							// reasons here
							// It MUST be made for security reasons on the
							// server side
							if (value instanceof JavaElement) {
								continue;
							}
							else if (value instanceof UDEDefinition) {
								UDEDefinition uded = (UDEDefinition) value;
								String path = null;
								try {
									path = uded.getEnv().getStack().getVarAsString("#path");
								}
								catch (VariableNotFoundException e) {
									// not defined
								}
								if (path != null && path.startsWith("@classpath")) {
									// Part of a system library
									continue;
								}
								else {
									DefUtil.addDef(copy, copy.currentFrame(),
											(String) prefix, name.substring(5), uded);
								}
							}
							else {
								throw new KarajanRuntimeException("Unrecognized definition type: "
										+ value);
							}
						}
					}
					else {
						copy.setVar(name, stack.getVar(name));
					}
				}
				else {
					try {
						copy.setVar(name, stack.getVar(name));
					}
					catch (VariableNotFoundException e) {
						// variable is not visible
					}
				}
			}
			catch (VariableNotFoundException e) {
				logger.error("System error #1", e);
			}
		}
		return copy;
	}
}
