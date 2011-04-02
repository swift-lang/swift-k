// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 10, 2003
 */
package org.globus.cog.karajan.viewer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.util.Queue;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.AbortException;
import org.globus.cog.karajan.workflow.events.ControlEvent;
import org.globus.cog.karajan.workflow.events.ControlEventType;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.EventHook;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.karajan.workflow.events.FlowEvent;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public class Hook implements EventHook, Runnable {

	private static Logger logger = Logger.getLogger(Hook.class);

	private HashMap globalFilters;
	private HashMap breakPoints;
	private HashMap elementFilters, activeFilters;
	private Set ignoredElements;
	private Set monitoredElements;
	private Queue failureQueue;
	private boolean done;
	private List actions;
	private ActionDialog actionDialog;
	private HookListener listener;

	public Hook() {
		this.ignoredElements = new HashSet();
		this.elementFilters = new HashMap();
		this.activeFilters = new HashMap();
		this.globalFilters = new HashMap();
		this.breakPoints = new HashMap();
		this.monitoredElements = new HashSet();
		this.actions = new LinkedList();
		this.actions.add(new NoAction());
		this.actions.add(new IgnoreErrors());
		this.actions.add(new RestartAction());
	}

	public void event(EventListener element, Event e) {
		try {
			if (listener != null) {
				listener.hookedEvent(e, element);
			}
		}
		catch (Exception ex) {
			logger.warn("Uncaught exception ", ex);
		}
		if (e instanceof ControlEvent) {
			controlEvent((FlowElement) element, (ControlEvent) e);
		}
		else if (e instanceof NotificationEvent) {
			notificationEvent(element, (NotificationEvent) e);
		}
		else {
			EventBus.send(element, e);
		}
	}

	public void controlEvent(FlowElement element, ControlEvent e) {
		if (e.getType().equals(ControlEventType.START)) {
			ThreadedUID te;
			try {
				te = new ThreadedUID((FlowElement) element, e);
				if (breakPoints.containsKey(te)) {
					breakPoints.put(te, e);
					fireBreakpointReached(te, e);
					return;
				}
			}
			catch (VariableNotFoundException e1) {
				logger.warn("Cannot create threaded element", e1);
			}
		}
		EventBus.send(element, e);
	}

	public void notificationEvent(EventListener element, NotificationEvent e) {
		//TODO
		if (/*(e instanceof ChainedFailureNotificationEvent) || */!(element instanceof FlowElement)) {
			EventBus.send(element, e);
			return;
		}
		try {
			if ((e.getType().equals(NotificationEventType.EXECUTION_COMPLETED))
					|| ignoredElements.contains(new ThreadedUID(e))) {
				EventBus.send(element, e);
				return;
			}
		}
		catch (VariableNotFoundException e1) {
			logger.warn("Could not create threaded element", e1);
		}
		if (e.getType().equals(NotificationEventType.EXECUTION_FAILED)) {
			if (((FailureNotificationEvent) e).getException() instanceof AbortException) {
				EventBus.send(element, e);
				return;
			}
			if (ignoredElements.contains(e.getFlowElement())) {
				EventBus.send(element, e);
				return;
			}
			failureQueue.enqueue(new ListenerEventPair(element, e));
			synchronized (this) {
				notify();
			}
			return;
		}
	}

	public void addBreakPoint(ThreadedUID el) {
		breakPoints.put(el, null);
	}

	public void removeBreakPoint(ThreadedUID el) {
		breakPoints.remove(el);
	}

	public void addGlobalFilter(String message, FailureAction fa) {
		globalFilters.put(message, fa);
	}

	public void removeGlobalFilter(String message) {
		globalFilters.remove(message);
	}

	public void ignoreElement(ThreadedUID el) {
		ignoredElements.add(el);
	}

	public void removeIgnore(ThreadedUID el) {
		ignoredElements.remove(el);
	}

	public void addElementFilter(ThreadedUID el, FailureAction fa) {
		elementFilters.put(el, fa);
	}

	public void removeElementFilter(ThreadedUID el) {
		elementFilters.remove(el);
	}

	public void addMonitoredElement(ThreadedUID el) {
		monitoredElements.add(el);
	}

	public void removeMonitoredElement(ThreadedUID el) {
		monitoredElements.remove(el);
	}

	public void run() {
		done = false;
		failureQueue = new Queue();
		while (!done) {
			if (failureQueue.isEmpty()) {
				synchronized (this) {
					try {
						wait(250);
					}
					catch (InterruptedException e) {
					}
				}
			}
			else {
				if (done) {
					break;
				}
				ListenerEventPair failure = (ListenerEventPair) failureQueue.dequeue();
				ThreadingContext thread;
				try {
					thread = ThreadingContext.get(failure.getEvent().getStack());
				}
				catch (VariableNotFoundException e) {
					logger.warn("Could not get thread for event", e);
					return;
				}
				ThreadedUID element = new ThreadedUID(failure.getEvent().getFlowElement(),
						thread);
				EventListener listener = failure.getListener();
				String message = ((FailureNotificationEvent) failure.getEvent()).getMessage();
				FailureAction fa;
				if (activeFilters.containsKey(element)) {
					fa = (FailureAction) activeFilters.get(element);
				}
				else if (elementFilters.containsKey(element)) {
					fa = ((FailureAction) elementFilters.get(element)).newInstance();
				}
				else if (globalFilters.containsKey(message)) {
					fa = ((FailureAction) globalFilters.get(message)).newInstance();
				}
				else if (ignoredElements.contains(element)) {
					fa = new NoAction();
				}
				else {
					fa = actionDialog(element, (FailureNotificationEvent) failure.getEvent()).newInstance();
				}
				executeAction(fa, listener, failure.getEvent());
				if (!fa.isComplete()) {
					activeFilters.put(element, fa);
				}
				else if (activeFilters.containsKey(element)) {
					activeFilters.remove(element);
				}
			}
		}
	}

	public void shutdown() {
		if (actionDialog != null) {
			actionDialog.close();
			actionDialog = null;
		}
		done = true;
	}

	private void executeAction(FailureAction fa, EventListener element, NotificationEvent event) {
		fa.handleFailure(element, event);
	}

	private FailureAction actionDialog(ThreadedUID element, FailureNotificationEvent event) {
		actionDialog = new ActionDialog(this, actions, element, event);
		logger.debug(event);
		FailureAction fa = actionDialog.choice();
		actionDialog = null;
		return fa;
	}

	public void setListener(HookListener listener) {
		this.listener = listener;
	}

	protected void fireBreakpointReached(ThreadedUID el, FlowEvent event) {
		listener.breakpointReached(el, event);
	}

	public void resumeElement(ThreadedUID element) {
		NotificationEvent e = (NotificationEvent) breakPoints.get(element);
		breakPoints.put(element, null);
		executeAction(new NoAction(), element.getElement(), e);
	}

}