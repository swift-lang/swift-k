// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.compiled.nodes.grid;

import java.util.HashMap;
import java.util.Map;

import k.rt.Channel;
import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.scheduler.Scheduler;
import org.globus.cog.karajan.scheduler.TaskTransformerFactory;
import org.globus.cog.karajan.util.ContactSet;
import org.globus.cog.karajan.util.KarajanProperties;
import org.globus.cog.karajan.util.Property;
import org.globus.cog.karajan.util.TaskHandlerWrapper;
import org.globus.cog.karajan.util.TypeUtil;

public class SchedulerNode extends InternalFunction {
	public static final Logger logger = Logger.getLogger(SchedulerNode.class);
	
	public static final String CONTEXT_ATTR_NAME = "TASK:SCHEDULER";
	
	private ArgRef<String> type;
	private ArgRef<ContactSet> resources;
	private ChannelRef<Property> c_properties;
	private ChannelRef<String> c_taskTransformers;
	private ChannelRef<TaskHandlerWrapper> c_handlers;

	private ArgRef<String> shareID;
	
	@Override
	protected Signature getSignature() {
		return new Signature(
				params(
						"type", 
						optional("resources", null), optional("shareID", null), 
						channel("properties"), channel("taskTransformers"), channel("handlers")));
	}

	private VarRef<Context> context;
	private VarRef<KarajanProperties> properties;

	static {
		sharedInstances = new HashMap<String, Scheduler>();
	}

	private static Map<String, String> schedulers;
	private static Map<String, Scheduler> sharedInstances;

	private void initializeSchedulers(KarajanProperties properties) {
		schedulers = new HashMap<String, String>();
		for (String name : properties.getPropertyNames()) {
			if (name.startsWith("scheduler.")) {
				schedulers.put(name.substring(10), 
						TypeUtil.toString(properties.get(name)));
			}
		}
	}

	@Override
	protected void addLocals(Scope scope) {
		context = scope.getVarRef("#context");
		properties = scope.getVarRef("#properties");
		super.addLocals(scope);
	}

	@Override
	public void runBody(LWThread thr) {
		Stack stack = thr.getStack();
		Scheduler s = null;
		String shareID = this.shareID.getValue(stack);
		if (shareID != null) {
			synchronized(sharedInstances) {
				s = sharedInstances.get(shareID);
				if (s == null) {
					s = newScheduler(stack);
					sharedInstances.put(shareID, s);
				}
			}
		}
		else {
			s = newScheduler(stack);
		}
		context.getValue(stack).setAttribute(CONTEXT_ATTR_NAME, s);
	}
	
	@SuppressWarnings("unchecked")
	protected Scheduler newScheduler(Stack stack) {
		Scheduler s;
		String type = this.type.getValue(stack);
		synchronized (SchedulerNode.class) {
			if (schedulers == null) {
				initializeSchedulers(properties.getValue(stack));
			}
		}
		if (schedulers.containsKey(type)) {
			try {
				Class<Scheduler> c = (Class<Scheduler>) this.getClass().getClassLoader().loadClass(schedulers.get(type));
				s = c.newInstance();
			}
			catch (Exception e) {
				throw new ExecutionException("Could not instantiate scheduler. " + e.getMessage(),
						e);
			}
		}
		else {
			throw new ExecutionException("No such scheduler " + type + ". Available schedulers: "
					+ schedulers.keySet());
		}

		Channel<Property> properties = this.c_properties.get(stack);
		for (Property entry : properties) {
			s.setProperty(entry.getKey(), entry.getValue());
		}

		Channel<String> taskTransformers = this.c_taskTransformers.get(stack);
		for (String cls : taskTransformers) {
			try {
				s.addTaskTransformer(TaskTransformerFactory.newFromClass(cls));
			}
			catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Error instantiating task transformer", e);
				}
				throw new ExecutionException(
						"Could not instantiate task transformer (" + cls + ")", e);
			}
		}

		
		ContactSet resources = this.resources.getValue(stack);
		if (resources != null) {
			s.setResources(resources);
		}

		for (TaskHandlerWrapper w : this.c_handlers.get(stack)) {
			s.addTaskHandler(w);
		}

		s.start();
		return s;
	}
}
