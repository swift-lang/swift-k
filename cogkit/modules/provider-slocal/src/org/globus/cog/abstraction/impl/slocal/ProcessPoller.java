/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
