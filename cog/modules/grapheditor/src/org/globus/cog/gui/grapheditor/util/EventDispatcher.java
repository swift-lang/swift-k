
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.util;

import java.util.Collections;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Queues and dispatches events to objects. It is used to allow the swing/awt thread
 * to continue execution when events are being handled.
 */
public class EventDispatcher extends Thread {
	private static Logger logger = Logger.getLogger(EventDispatcher.class);

	private static List q, p;
	private static boolean finished;
	private static boolean initialized = false;

	public EventDispatcher() {
		q = Collections.synchronizedList(new LinkedList());
		p = new LinkedList();
		finished = false;
	}

	public static void queue(EventConsumer ec, EventObject e) {
		if (!initialized) {
			EventDispatcher ed = new EventDispatcher();
			ed.start();
			initialized = true;
		}
		synchronized (q) {
			q.add(e);
			p.add(ec);
		}
	}

	public void run() {
		while (!finished) {
			while (q.size() != 0) {
				Object listener = null;
				Object event = null;
				synchronized (q) {
					listener = p.remove(0);
					event = q.remove(0);
				}
				try {
					((EventConsumer) listener).event((EventObject) event);
				}
				catch (Exception ex) {
					logger.warn("Uncaught exception: ", ex);
				}

			}
			try {
				sleep(100);
			}
			catch (InterruptedException ie) {
			}
		}
	}
}
