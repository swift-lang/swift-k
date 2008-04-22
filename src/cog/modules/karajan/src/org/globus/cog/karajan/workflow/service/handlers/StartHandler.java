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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.execution.gt2.GlobusSecurityContextImpl;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.NamedArgumentsImpl;
import org.globus.cog.karajan.stack.LinkedStack;
import org.globus.cog.karajan.stack.StackFrame;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.DefList;
import org.globus.cog.karajan.util.DefinitionEnvironment;
import org.globus.cog.karajan.util.KarajanProperties;
import org.globus.cog.karajan.util.serialization.XMLConverter;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.JavaElement;
import org.globus.cog.karajan.workflow.nodes.Disallowed;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.Include;
import org.globus.cog.karajan.workflow.nodes.user.UDEDefinition;
import org.globus.cog.karajan.workflow.service.InstanceContext;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.RemoteContainer;
import org.globus.cog.karajan.workflow.service.RemoteExecutionContext;
import org.globus.cog.karajan.workflow.service.RemoteNode;
import org.globus.cog.karajan.workflow.service.RemoteVariableArguments;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;

public class StartHandler extends RequestHandler {
	private static final Logger logger = Logger.getLogger(StartHandler.class);
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
			if (logger.isDebugEnabled()) {
				logger.debug("Destination UID: " + eid);
			}
			Inflater inflater = new Inflater();
			InputStreamReader isr = new InputStreamReader(new InflaterInputStream(
					new ByteArrayInputStream((byte[]) data.get(2)), inflater));
			try {
				Object o;
				if (ic.getChannelContext().getService().isRestricted()) {
					o = XMLConverter.readObjectRestricted(isr, ic.getTree());
				}
				else {
					o = XMLConverter.readObject(isr, ic.getTree());
				}
				if (o instanceof VariableStack) {
					VariableStack stack = prepareStack((VariableStack) o);
					FlowElement fe = ic.getTree().getUIDMap().get(eid);
					fe = addImports(fe, stack, ic.getUserContext().getChannelContext());
					// stack.getExecutionContext().getTree().setRoot(fe);
					ic.getUserContext().getChannelContext().initialize();
					((RemoteExecutionContext) stack.getExecutionContext()).start(stack, fe);
					sendReply("OK".getBytes());
				}
				else {
					sendError("Invalid stack");
				}
			}
			catch (Exception e) {
				sendError(e.getMessage(), e);
			}
		}
	}

	private FlowElement addImports(FlowElement fe, VariableStack stack, ChannelContext sc)
			throws ExecutionException {
		RemoteContainer seq = new RemoteContainer();
		seq.setElementType("sequential");
		if (stack.isDefined("#imports")) {
			Collection imports = (Collection) stack.getVar("#imports");
			Iterator i = imports.iterator();
			while (i.hasNext()) {
				String imp = (String) i.next();
				seq.addElement(getImport(sc, imp));
			}
		}
		seq.addElement(fe);
		seq.setProperty(FlowElement.UID, fe.getProperty(FlowElement.UID));
		seq.loadComplete();
		return seq;
	}

	public static final String IMPORTS = "imports";

	private synchronized FlowElement getImport(ChannelContext cc, String imp) {
		Map imports = (Map) cc.getData(IMPORTS);
		if (imports == null) {
			imports = new HashMap();
			cc.addData(IMPORTS, imports);
		}
		return getImport(imports, imp);
	}

	public FlowElement getImport(Map imports, String imp) {
		synchronized (imports) {
			if (!imports.containsKey(imp)) {
				Include incl = new Include();
				incl.setElementType("import");
				Include.A_FILE.setStatic(incl, imp);
				incl.setProperty(FlowElement.UID, new Integer(-1));
				incl.loadComplete();
				imports.put(imp, incl);
				return incl;
			}
			else {
				return (FlowElement) imports.get(imp);
			}
		}
	}

	private void addRemoteChannel(VariableStack stack, Arg.Channel channel) {
		ArgUtil.createChannel(stack, channel, new RemoteVariableArguments(channel, ic));
	}

	protected VariableStack prepareStack(VariableStack stack) throws ExecutionException {
		Set names = new HashSet();
		Integer caller = (Integer) stack.getVar("#calleruid");
		KarajanProperties properties;
		if (ic.getChannelContext().getService().isRestricted()) {
			properties = KarajanProperties.getRestricted();
		}
		else {
			properties = KarajanProperties.getDefault();
		}
		VariableStack copy = new LinkedStack(new RemoteExecutionContext(ic, caller.intValue(),
				properties));
		// Mark the current environment as untrusted if running in restricted
		// mode
		copy.setVar("#trusted", !ic.getChannelContext().getService().isRestricted());

		GlobusSecurityContextImpl sc = new GlobusSecurityContextImpl();
		// Although not "correct", this will be used for now.
		sc.setCredentials(ic.getChannelContext().getUserContext().getName());
		copy.setVar("#securitycontext", sc);
		names.addAll(stack.firstFrame().names());
		for (int i = 1; i < stack.frameCount(); i++) {
			StackFrame frame = stack.getFrame(i);
			names.addAll(frame.names());
		}
		copy.enter();
		copy.setVar(RemoteNode.REMOTE_FLAG, true);
		/* 
		 * The stack on this will be set by the remote container
		 * It is needed because the remote imports, which are needed
		 * by user defined elements, are not on this frame 
		 */
		DefinitionEnvironment env = new DefinitionEnvironment(null, null);
		copy.setVar(RemoteContainer.DEF_ENV, env);
		
		Collection channels = (Collection) stack.getVar("#channels");
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			addRemoteChannel(copy, (Arg.Channel) i.next());
		}
		Iterator ni = names.iterator();
		while (ni.hasNext()) {
			String name = (String) ni.next();
			try {
				if (name.startsWith("##")) {
					continue;
				}
				else if (name.startsWith("#")) {
					if (name.equals("#vargs")) {
						addRemoteChannel(copy, Arg.VARGS);
					}
					else if (name.equals("#nargs")) {
						// these don't go over for now
						copy.setVar(ArgUtil.NARGS, new NamedArgumentsImpl());
					}
					else if (name.startsWith("#channel#")) {
						// already added from the channel list
						// addRemoteChannel(copy, name);
					}
					else if (name.startsWith("#def#")) {
						// allow only user defined elements
						DefList m = (DefList) stack.getVar(name);
						Iterator ei = m.prefixes().iterator();
						while (ei.hasNext()) {
							String prefix = (String) ei.next();
							Object value = m.get(prefix);
							if (value instanceof JavaElement) {
								m.put(prefix, new Disallowed());
							}
							else if (value instanceof UDEDefinition) {
								UDEDefinition uded = (UDEDefinition) value;
								m.put(prefix, new UDEDefinition(uded.getUdeNR(), env));
							}
						}
						copy.setVar(name, m);
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
				logger.error("System error #1");
			}
		}
		return copy;
	}
}
