// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jun 6, 2003
 *  
 */
package org.globus.cog.karajan.workflow.events;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public final class EventBus {
	public static final Logger logger = Logger.getLogger(EventBus.class);

	private static final EventBus bus = new EventBus();
	public volatile static long eventCount;
	public volatile static long cummulativeEventTime;

	private final Queues events;
	private final WorkerManager workers;

	private final EventDispatcher dispatcher;
	
	private static EventHook hook;

	public EventBus() {
		events = new Queues();
		workers = new WorkerManager();
		dispatcher = EventDispatcher.newDispatcher(events, workers);
		dispatcher.start();
	}

	private void _post(EventListener target, Event event) {
		EventTargetPair etp = new EventTargetPair(event, target);
		events.enqueue(etp);
	}

	private void _suspendAll() {
		dispatcher.suspendAll();
	}

	private void _resumeAll() {
		dispatcher.resumeAll();
	}

	public static void post(EventListener target, Event event) {
		eventCount++;
		bus._post(target, event);
	}
	
	public static EventBus getBus() {
		return bus;
	}
	
	public WorkerManager getWorkerManager() {
		return workers; 
	}
	
	public Queues getQueues() {
		return events;
	}

	public synchronized static void initialize() {
	}

	public static void suspendAll() {
		bus._suspendAll();
	}

	public static void resumeAll() {
		bus._resumeAll();
	}

	public static boolean isInitialized() {
		return true;
	}

	public static void setEventHook(EventHook hook) {
		EventBus.hook = hook;
	}

	public static void removeEventHook() {
		EventBus.hook = null;
	}

	public static void sendHooked(final EventListener l, final Event e) {
		if (hook == null) {
			send(l, e);
		}
		else {
			EventHook h = hook;
			if (h != null) {
				try {
					hook.event(l, e);
				}
				catch (Exception ex) {
					logger.warn("Bogus hook (" + hook.getClass().getName()
							+ ") threw exception. Bypassing.", ex);
					send(l, e);
				}
			}
			else {
				send(l, e);
			}
		}
	}

	public static void send(final EventListener l, final Event event) {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug(event + " -> " + l);
			}
			if (l != null) {
				l.event(event);
			}
			else {
				logger.warn("Got event with no destination: " + event);
			}
		}
		catch (ExecutionException e) {
			if (logger.isInfoEnabled()) {
				logger.info("Caught execution exception", e);
			}
			try {
				if (event instanceof FlowEvent) {
					failElement(l, (FlowEvent) event, e.getMessage());
				}
				else {
					logger.warn("ExecutionException caught, but event is not a flow event");
				}
			}
			catch (Exception ee) {
				logger.fatal("Cannot fail element", ee);
				logger.fatal("Exception was", e);
			}
		}
		catch (Exception e) {
			try {
				logger.warn("Uncaught exception: " + e.toString() + " in " + l, e);
				logger.warn("Event was " + event);
				logger.warn("Exception is: " + e);
				try {
					if (event instanceof FlowEvent) {
						failElement(l, (FlowEvent) event, "Uncaught exception: " + e.toString());
					}
				}
				catch (Exception ee) {
					logger.fatal("Cannot fail element", ee);
				}
			}
			catch (Exception ee) {
				logger.warn("Another uncaught exception while handling an uncaught exception.", ee);
				logger.warn("The initial exception was", e);
				try {
					if (event instanceof FlowEvent) {
						// this try time to fail the sender of the event
						failElement(null, (FlowEvent) event, "Double uncaught exception: "
								+ e.toString() + "\n" + ee.toString());
					}
				}
				catch (Exception eee) {
					logger.fatal("Cannot fail element", ee);
				}
			}
		}
	}

	public static void failElement(EventListener l, FlowEvent event, String message)
			throws ExecutionException {
		VariableStack stack = event.getStack().copy();
		FlowElement fe;
		if (l instanceof FlowElement) {
			fe = (FlowElement) l;
		}
		else {
			fe = event.getFlowElement();
		}
		fe.failImmediately(stack, message);
	}

	public static boolean waitForEvents() {
		boolean busy = true;
		int count = 0;
		while (busy) {
			if (!bus.dispatcher.isSuspended()) {
				throw new KarajanRuntimeException(
						"EventBus.waitForEvents() called with an unsuspended bus. Call EventBus.suspendAll() first");
			}
			if (bus.workers.getWorking().size() == 0) {
				busy = false;
			}
			if (busy) {
				if (count == 20) {
					logger.warn("Waited one second for events to be processed. Still busy");
				}
				if (count == 100) {
					logger.warn("Waited five second for events to be processed. Still busy");
				}
				if (count == 200) {
					logger.warn("Waited ten seconds for events to be processed. Still busy. Failing");
					return false;
				}
				try {
					Thread.sleep(50);
				}
				catch (InterruptedException e) {
				}
			}
			count++;
		}
		return true;
	}

	public static Collection getAllEvents() {
		if (!bus.dispatcher.isSuspended()) {
			throw new KarajanRuntimeException(
					"EventBus.getAllEvents() called with an unsuspended bus. Call EventBus.suspendAll() first");
		}
		return bus.dispatcher.getAllEvents();
	}

	protected static EventDispatcher getDispatcher() {
		return bus.dispatcher;
	}
}