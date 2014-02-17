// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.scheduler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Contact;
import org.globus.cog.karajan.util.ContactSet;
import org.globus.cog.karajan.util.Queue;
import org.globus.cog.karajan.util.TaskHandlerWrapper;
import org.globus.cog.karajan.util.TypeUtil;

public abstract class AbstractScheduler extends Thread implements Scheduler {
	private static final Logger logger = Logger.getLogger(AbstractScheduler.class);
	
	public static final int THROTTLE_OFF = Integer.MAX_VALUE;

	private List<TaskHandlerWrapper> taskHandlers;

	private final Map<Integer, Map<String, TaskHandlerWrapper>> handlerMap;

	private int maxSimultaneousJobs;

	private final Queue<Entry> jobs;

	private final Map<Task, Entry> entries;

	private ContactSet grid;

	private final Map<String, Object> properties;

	private List<TaskTransformer> taskTransformers;
	private List<FailureHandler> failureHandlers;

	private ResourceConstraintChecker constraintChecker;

	public AbstractScheduler() {
		super("Scheduler");
		jobs = new Queue<Entry>();
		entries = new HashMap<Task, Entry>();
		handlerMap = new HashMap<Integer, Map<String, TaskHandlerWrapper>>();
		grid = new ContactSet();
		maxSimultaneousJobs = 65536;
		properties = new HashMap<String, Object>();
		taskTransformers = new LinkedList<TaskTransformer>();
		failureHandlers = new LinkedList<FailureHandler>();

		// We always have the local file provider
		TaskHandlerWrapper local = new TaskHandlerWrapper();
		local.setProvider("local");
		local.setType(TaskHandler.FILE_OPERATION);
		this.addTaskHandler(local);
	}

	public final void addTaskHandler(TaskHandlerWrapper taskHandler) {
		if (taskHandlers == null) {
			taskHandlers = new LinkedList<TaskHandlerWrapper>();
		}
		taskHandlers.add(taskHandler);
		Map<String, TaskHandlerWrapper> ht = handlerMap.get(taskHandler.getType());
		if (ht == null) {
			ht = new HashMap<String, TaskHandlerWrapper>();
			handlerMap.put(taskHandler.getType(), ht);
		}
		ht.put(taskHandler.getProvider(), taskHandler);
	}

	public List<TaskHandlerWrapper> getTaskHandlers() {
		return taskHandlers;
	}

	public void setTaskHandlers(List<TaskHandlerWrapper> taskHandlers) {
		this.taskHandlers = taskHandlers;
	}

	public TaskHandlerWrapper getTaskHandlerWrapper(int index) {
		return getTaskHandlers().get(index);
	}

	public Collection<TaskHandlerWrapper> getTaskHandlerWrappers(int type) {
		if (handlerMap.containsKey(type)) {
			return handlerMap.get(type).values();
		}
		else {
			return Collections.emptyList();
		}
	}

	public TaskHandlerWrapper getTaskHandlerWrapper(int type, String provider) {
	    Map<String, TaskHandlerWrapper> pm = handlerMap.get(type);
	    if (pm == null) {
	        return null;
	    }
	    else {
	        return pm.get(provider);
	    }
	}

	public void setResources(ContactSet grid) {
		if (logger.isInfoEnabled()) {
			logger.info("Setting resources to: " + grid);
		}
		this.grid = grid;
	}

	public ContactSet getResources() {
		return grid;
	}
	
	protected void setEntry(Task t, Entry e) {
		synchronized(entries) {
			entries.put(t, e);
		}
	}
	
	protected Entry removeEntry(Task t) {
		synchronized(entries) {
			return entries.remove(t);
		}
	}
	
	protected Entry getEntry(Task t) {
		synchronized(entries) {
			return entries.get(t);
		}
	}
	
	public Object getConstraints(Task t) {
		return getEntry(t).constraints;
	}

	public void fireJobStatusChangeEvent(final StatusEvent ev, Entry e) {
		if (e.listener != null) {
			e.listener.statusChanged(ev);
		}
	}

	public void fireJobStatusChangeEvent(Entry e, Status status) {
		StatusEvent jsce = new StatusEvent(e.task, status);
		fireJobStatusChangeEvent(jsce, e);
	}

	public int getMaxSimultaneousJobs() {
		return maxSimultaneousJobs;
	}

	public void setMaxSimultaneousJobs(int i) {
		maxSimultaneousJobs = i;
	}

	public Queue<Entry> getJobQueue() {
		return jobs;
	}

	public void setProperty(String name, Object value) {
		if (name.equalsIgnoreCase("maxSimultaneousJobs")) {
			logger.debug("Scheduler: setting maxSimultaneousJobs to " + value);
			setMaxSimultaneousJobs(throttleValue(value));
		}
		else {
			throw new IllegalArgumentException("Unsupported property: " + name
					+ ". Supported properties are: " + Arrays.asList(this.getPropertyNames()));
		}
	}
	
	protected int throttleValue(Object value) {
	    if ("off".equalsIgnoreCase(value.toString())) {
	        return THROTTLE_OFF;
	    }
	    else {
	        return TypeUtil.toInt(value);
	    }
	}
	
	protected float floatThrottleValue(Object value) {
	    if ("off".equalsIgnoreCase(value.toString())) {
	        return THROTTLE_OFF;
	    }
	    else {
	        return (float) TypeUtil.toDouble(value);
	    }
	}

	public Object getProperty(String name) {
		return properties.get(name);
	}

	public static final String[] propertyNames = new String[] { "maxSimultaneousJobs" };

	public String[] getPropertyNames() {
		return propertyNames;
	}

	public static String[] combineNames(String[] first, String[] last) {
		String[] combined = new String[first.length + last.length];
		System.arraycopy(first, 0, combined, 0, first.length);
		System.arraycopy(last, 0, combined, first.length, last.length);
		return combined;
	}

	public void addTaskTransformer(TaskTransformer taskTransformer) {
		this.taskTransformers.add(taskTransformer);
	}

	public List<TaskTransformer> getTaskTransformers() {
		return taskTransformers;
	}

	protected void applyTaskTransformers(Task t, Contact[] contacts, Service[] services) {
		for (TaskTransformer tt : getTaskTransformers()) {
			tt.transformTask(t, contacts, services);
		}
	}

	protected boolean runFailureHandlers(Entry e) {
		for (FailureHandler fh : failureHandlers) {
			if (fh.handleFailure(e, this)) {
				return true;
			}
		}
		return false;
	}

	public void addFailureHandler(FailureHandler handler) {
		failureHandlers.add(handler);
	}

	public ResourceConstraintChecker getConstraintChecker() {
		return constraintChecker;
	}

	public void setConstraintChecker(ResourceConstraintChecker constraintChecker) {
		this.constraintChecker = constraintChecker;
	}

	protected boolean checkConstraints(BoundContact resource, TaskConstraints tc) {
		if (constraintChecker == null) {
			return true;
		}
		else {
			return constraintChecker.checkConstraints(resource, tc);
		}
	}
	
	public final void statusChanged(StatusEvent se) {
		Task task = (Task) se.getSource();
		if (task == null) {
			logger.warn("Got status event with no task " + se);
			return;
		}
		Entry e;
		if (se.getStatus().isTerminal()) {
			e = removeEntry(task);
		}
		else {
			e = getEntry(task);
		}
		
		if (e == null) {
			logger.warn("Got status event for task with no entry" + task);
			return;
		}
		statusChanged(se, e);
	}
	
	protected abstract void statusChanged(StatusEvent se, Entry e);
}
