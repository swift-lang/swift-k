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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

/**
 * <p>
 * Lightweight threading in Karajan is implemented using events. The major
 * events are {@link ControlEvent control events}, which are sent to elements in
 * order to command a certain execution behavior, and {@link NotificationEvent
 * notification events} which are used to report on the status of the execution
 * of elements.
 * </p>
 * 
 * <p>
 * Events are sent to element using an event bus, which in turn uses a fixed
 * number of worker threads to do the actual dispatch. This is needed since most
 * elements do their actual work as part of the event handling code. As such,
 * the <code>EventBus</code> can be seen as a form of
 * {@link java.util.concurrent.ExecutorService}.
 * <p>
 * 
 * @author Mihael Hategan
 * 
 */
public final class EventBus {
	public static final Logger logger = Logger.getLogger(EventBus.class);

	public static final int DEFAULT_WORKER_COUNT = Runtime.getRuntime().availableProcessors() * 2;

	private static final EventBus bus = new EventBus();
	public volatile static long eventCount;

	private final ThreadPoolExecutor es;

	public EventBus() {
		es = new ThreadPoolExecutor(DEFAULT_WORKER_COUNT, DEFAULT_WORKER_COUNT,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());		
	}

	private void _post(FlowElement target, VariableStack stack) {
		EventTargetPair etp = new EventTargetPair(stack, target);
		es.submit(etp);
	}
	
	private void _post(Runnable r) {
            es.submit(r);
    }

	/**
	 * Post an event to the bus. The event will be placed in a queue and
	 * delivered in FIFO order using available worker threads.
	 */
	public static void post(FlowElement target, VariableStack stack) {
		eventCount++;
		bus._post(target, stack);
	}
	
	public boolean isAnythingRunning() {
		return es.getActiveCount() != 0;
	}
	
	public static void post(Runnable r) {
	    eventCount++;
	    bus._post(r);
	}

	public static EventBus getBus() {
		return bus;
	}

	public synchronized static void initialize() {
	}

	public static boolean isInitialized() {
		return true;
	}

	/**
	 * Directly send an event to a destination. Typically the worker threads
	 * would use this method rather than it being used directly.
	 */
	public static void start(final FlowElement l, final VariableStack stack) {
		try {
			//System.out.println(l);
			l.start(stack);
		}
		catch (ExecutionException e) {
			if (logger.isInfoEnabled()) {
				logger.info("Caught execution exception", e);
				logger.info("Near Karajan line: " + l);
			}
			try {
				failElement(l, stack, e.getMessage());
			}
			catch (Exception ee) {
				logger.fatal("Cannot fail element", ee);
				logger.fatal("Exception was", e);
			}
		}
		catch (Throwable e) {
			try {
				logger.warn("Uncaught exception: " + e.toString() + " in " + l, e);
				logger.warn("Exception is: " + e);
				logger.warn("Near Karajan line: " + l);
				try {
					failElement(l, stack, "Uncaught exception: " + e.toString());
				}
				catch (Exception ee) {
					logger.fatal("Cannot fail element", ee);
				}
			}
			catch (Throwable ee) {
				logger.warn("Another uncaught exception while handling an uncaught exception.", ee);
				logger.warn("The initial exception was", e);
				try {
					// this try time to fail the sender of the event
					failElement(null, stack, "Double uncaught exception: "
								+ e.toString() + "\n" + ee.toString());
				}
				catch (Throwable eee) {
					logger.fatal("Cannot fail element", ee);
				}
			}
		}
	}

	public static void failElement(FlowElement l, VariableStack stack, String message)
			throws ExecutionException {
		l.failImmediately(stack, new ExecutionException(message));
	}
}