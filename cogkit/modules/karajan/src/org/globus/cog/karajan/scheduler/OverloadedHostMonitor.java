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
 * Created on Jul 21, 2008
 */
package org.globus.cog.karajan.scheduler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

public class OverloadedHostMonitor extends Thread {
	public static final Logger logger = Logger.getLogger(OverloadedHostMonitor.class);

	public static final int POLL_INTERVAL = 1000;
	private Set<WeightedHost> hosts;
	private WeightedHostScoreScheduler whss;

	private static final Integer[] DIRS = new Integer[] { new Integer(-1), new Integer(0),
			new Integer(1) };

	public OverloadedHostMonitor(WeightedHostScoreScheduler whss) {
		super("Overloaded Host Monitor");
		setDaemon(true);
		hosts = new HashSet<WeightedHost>();
		this.whss = whss;
		start();
	}

	public void add(WeightedHost wh) {
		synchronized (hosts) {
			if (!hosts.contains(wh)) {
				hosts.add(wh);
			}
		}
	}

	public void run() {
		try {
			while (true) {
				Thread.sleep(POLL_INTERVAL);
				try {
					check();
				}
				catch (Exception e) {
					logger.warn("Exception caught while polling hosts", e);
				}
			}
		}
		catch (InterruptedException e) {
			logger.info("Interrupted", e);
		}
	}

	private void check() {
		synchronized (hosts) {
			Iterator<WeightedHost> i = hosts.iterator();
			while (i.hasNext()) {
				WeightedHost wh = i.next();
				if (wh.isOverloaded() == 0) {
					whss.removeOverloaded(wh);
					i.remove();
				}
			}
		}
	}
}
