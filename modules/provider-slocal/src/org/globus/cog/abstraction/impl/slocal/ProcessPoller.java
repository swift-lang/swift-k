//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.slocal;

import java.util.LinkedList;
import java.util.ListIterator;

public class ProcessPoller extends Thread {
	private LinkedList processors;
	private ListIterator p;
	boolean any = false;

	public ProcessPoller() {
		setName("Local provider stream poller");
		setDaemon(true);
		processors = new LinkedList();
	}

	public void addProcessor(StreamProcessor processor) {
		synchronized (processors) {
			if (p != null) {
				p.add(processor);
			}
			else {
				processors.add(processor);
			}
		}
	}

	public void run() {
		p = processors.listIterator();
		boolean empty;
		while (true) {
			while (processors.size() == 0) {
				try {
					Thread.sleep(250);
				}
				catch (InterruptedException e) {
				}
			}
			StreamProcessor sp;
			synchronized (processors) {
				if (!p.hasNext()) {
					p = processors.listIterator();
				}
				sp = (StreamProcessor) p.next();
				if (!sp.poll()) {
					p.remove();
				}
				else {
					any = true;
				}
			}
			if (!p.hasNext() && !any) {
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
				}
			}
		}

	}
}
