// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.scheduler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
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

	private List taskHandlers;

	private final Map handlerMap;

	private int maxSimultaneousJobs;

	private final Queue jobs;

	private final Map listeners;

	private ContactSet grid;

	private final Map properties;

	private final Map constraints;

	private List taskTransformers, failureHandlers;
	
	private ResourceConstraintChecker constraintChecker;

	public AbstractScheduler() {
		super("Scheduler");
		jobs = new Queue();
		listeners = new HashMap();
		handlerMap = new HashMap();
		grid = new ContactSet();
		maxSimultaneousJobs = 16384;
		properties = new HashMap();
		constraints = new HashMap();
		taskTransformers = new LinkedList();
		failureHandlers = new LinkedList();

		// We always have the local file provider
		TaskHandlerWrapper local = new TaskHandlerWrapper();
		local.setProvider("local");
		local.setType(TaskHandler.FILE_OPERATION);
		this.addTaskHandler(local);
	}

	public final void addTaskHandler(TaskHandlerWrapper taskHandler) {
		if (taskHandlers == null) {
			taskHandlers = new LinkedList();
		}
		taskHandlers.add(taskHandler);
		Integer type = new Integer(taskHandler.getType());
		Map ht = (Map) handlerMap.get(type);
		if (ht == null) {
			ht = new HashMap();
			handlerMap.put(type, ht);
		}
		ht.put(taskHandler.getProvider(), taskHandler);
	}

	public List getTaskHandlers() {
		return taskHandlers;
	}

	public void setTaskHandlers(List taskHandlers) {
		this.taskHandlers = taskHandlers;
	}

	public TaskHandlerWrapper getTaskHadlerWrapper(int index) {
		return (TaskHandlerWrapper) getTaskHandlers().get(index);
	}

	public Collection getTaskHandlerWrappers(int type) {
		Integer itype = new Integer(type);
		if (handlerMap.containsKey(itype)) {
			return ((Map) handlerMap.get(itype)).values();
		}
		else {
			return new LinkedList();
		}
	}

	public TaskHandlerWrapper getTaskHandlerWrapper(int type, String provider) {
		Integer itype = new Integer(type);
		if (handlerMap.containsKey(itype)) {
			return (TaskHandlerWrapper) ((Map) handlerMap.get(itype)).get(provider);
		}
		return null;
	}

	public void setResources(ContactSet grid) {
		this.grid = grid;
	}

	public ContactSet getResources() {
		return grid;
	}

	public void addJobStatusListener(StatusListener l, Task task) {
		List jobListeners;
		if (listeners.containsKey(task)) {
			jobListeners = (List) listeners.get(task);
		}
		else {
			jobListeners = new LinkedList();
			listeners.put(task, jobListeners);
		}
		jobListeners.add(l);
	}

	public void removeJobStatusListener(StatusListener l, Task task) {
		if (listeners.containsKey(task)) {
			List jobListeners = (List) listeners.get(task);
			jobListeners.remove(l);
			if (jobListeners.size() == 0) {
				listeners.remove(task);
			}
		}
	}

	public void fireJobStatusChangeEvent(StatusEvent e) {
		if (listeners.containsKey(e.getSource())) {
			List jobListeners = new LinkedList((List) listeners.get(e.getSource()));
			Iterator i = jobListeners.iterator();
			while (i.hasNext()) {
				((StatusListener) i.next()).statusChanged(e);
			}
		}
	}

	public void fireJobStatusChangeEvent(Task source, Status status) {
		StatusEvent jsce = new StatusEvent(source, status);
		fireJobStatusChangeEvent(jsce);
	}

	public int getMaxSimultaneousJobs() {
		return maxSimultaneousJobs;
	}

	public void setMaxSimultaneousJobs(int i) {
		maxSimultaneousJobs = i;
	}

	public Queue getJobQueue() {
		return jobs;
	}

	public void setProperty(String name, Object value) {
		if (name.equalsIgnoreCase("maxSimultaneousJobs")) {
			logger.debug("Scheduler: setting maxSimultaneousJobs to " + value);
			setMaxSimultaneousJobs(TypeUtil.toInt(value));
		}
		else {
			// properties.put(name, value);
			throw new IllegalArgumentException("Unsupported property: " + name
					+ ". Supported properties are: " + Arrays.asList(this.getPropertyNames()));
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

	protected void setConstraints(Task task, Object constraint) {
		synchronized(constraints) {
			constraints.put(task, constraint);
		}
	}

	protected Object getConstraints(Task task) {
		synchronized(constraints) {
			return constraints.get(task);
		}
	}

	public void addTaskTransformer(TaskTransformer taskTransformer) {
		this.taskTransformers.add(taskTransformer);
	}

	public List getTaskTransformers() {
		return taskTransformers;
	}

	protected void applyTaskTransformers(Task t, Contact[] contacts, Service[] services) {
		List transformers = getTaskTransformers();
		Iterator i = transformers.iterator();
		while (i.hasNext()) {
			((TaskTransformer) i.next()).transformTask(t, contacts, services);
		}
	}
	
	protected boolean runFailureHandlers(Task t) {
		Iterator i = failureHandlers.iterator();
		while (i.hasNext()) {
			FailureHandler fh = (FailureHandler) i.next();
			if (fh.handleFailure(t, this)) {
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
	
	protected List checkConstraints(List resources, TaskConstraints tc) {
		if (constraintChecker == null) {
			return resources;
		}
		else {
			return constraintChecker.checkConstraints(resources, tc);
		}
	}
}
