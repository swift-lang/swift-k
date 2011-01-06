// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes.grid;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.scheduler.Scheduler;
import org.globus.cog.karajan.scheduler.TaskTransformerFactory;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ContactSet;
import org.globus.cog.karajan.util.KarajanProperties;
import org.globus.cog.karajan.util.TaskHandlerWrapper;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.SequentialWithArguments;

public class SchedulerNode extends SequentialWithArguments {
	public static final Logger logger = Logger.getLogger(SchedulerNode.class);

	public static final Arg A_TYPE = new Arg.Positional("type", 0);
	public static final Arg A_SHARE_ID = new Arg.Optional("shareID");
	public static final Arg A_RESOURCES = new Arg.Positional("resources", 1);
	public static final Arg.Channel A_PROPERTIES = new Arg.Channel("properties");
	public static final Arg.Channel A_TASK_TRANSFORMERS = new Arg.Channel("taskTransformers");
	public static final Arg.Channel A_HANDLERS = new Arg.Channel("handlers");

	public static final String SCHEDULER = "task:scheduler";

	static {
		setArguments(SchedulerNode.class, new Arg[] { A_TYPE, A_RESOURCES, A_PROPERTIES,
				A_TASK_TRANSFORMERS, A_HANDLERS, A_SHARE_ID });
		sharedInstances = new HashMap();
	}

	private static Map schedulers;
	private static Map sharedInstances;

	private void initializeSchedulers(KarajanProperties properties) {
		schedulers = new HashMap();
		Iterator i = properties.keySet().iterator();
		while (i.hasNext()) {
			String name = (String) i.next();
			if (name.startsWith("scheduler.")) {
				schedulers.put(name.substring(10), properties.get(name));
			}
		}
	}

	public void post(VariableStack stack) throws ExecutionException {
		Scheduler s = null;
		String shareID = TypeUtil.toString(A_SHARE_ID.getValue(stack, null));
		if (shareID != null) {
			synchronized(sharedInstances) {
				s = (Scheduler) sharedInstances.get(shareID);
				if (s == null) {
					s = newScheduler(stack);
					sharedInstances.put(shareID, s);
				}
			}
		}
		else {
			s = newScheduler(stack);
		}
		stack.parentFrame().setVar(SCHEDULER, s);
		super.post(stack);
	}
	
	protected Scheduler newScheduler(VariableStack stack) throws ExecutionException {
		Scheduler s;
		String type = TypeUtil.toString(A_TYPE.getValue(stack));
		synchronized (SchedulerNode.class) {
			if (schedulers == null) {
				initializeSchedulers(stack.getExecutionContext().getProperties());
			}
		}
		if (schedulers.containsKey(type)) {
			try {
				Class c = this.getClass().getClassLoader().loadClass((String) schedulers.get(type));
				s = (Scheduler) c.newInstance();
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

		Iterator i;
		i = A_PROPERTIES.get(stack).iterator();
		while (i.hasNext()) {
			Map.Entry entry = (Map.Entry) i.next();
			Object key = checkClass(entry.getKey(), String.class, "string");
			s.setProperty((String) key, TypeUtil.toString(entry.getValue()));
		}

		i = A_TASK_TRANSFORMERS.get(stack).iterator();
		while (i.hasNext()) {
			String cls = (String) i.next();
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

		if (A_RESOURCES.isPresent(stack)) {
			s.setResources((ContactSet) A_RESOURCES.getValue(stack));
		}

		i = A_HANDLERS.get(stack).iterator();
		while (i.hasNext()) {
			s.addTaskHandler((TaskHandlerWrapper) i.next());
		}

		return s;
	}
}
