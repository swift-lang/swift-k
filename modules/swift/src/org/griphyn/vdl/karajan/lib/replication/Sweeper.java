/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on May 1, 2008
 */
package org.griphyn.vdl.karajan.lib.replication;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;


public class Sweeper extends Thread {
	public static final Logger logger = Logger.getLogger(Sweeper.class);
	
	public static final int SWEEP_INTERVAL = 10000;
	private static Sweeper sweeper;

	public synchronized static Sweeper getSweeper() {
		if (sweeper == null) {
			sweeper = new Sweeper();
			sweeper.start();
		}
		return sweeper;
	}

	private List stats;

	public Sweeper() {
		setName("Queued Task Replication Sweeper");
		setDaemon(true);
		stats = new ArrayList();
	}

	public void register(ReplicationManager taskStatistics) {
		synchronized (stats) {
			stats.add(taskStatistics);
		}
	}

	public void run() {
		try {
			while (true) {
				Thread.sleep(SWEEP_INTERVAL);
				List s;
				synchronized (stats) {
					s = new ArrayList(stats);
				}
				Iterator i = s.iterator();
				while (i.hasNext()) {
					try {
						((ReplicationManager) i.next()).checkTasks();
					}
					catch (Exception e) {
						logger.warn("Failed to check queued tasks", e);
					}
				}
			}
		}
		catch (InterruptedException e) {
		}
	}
}