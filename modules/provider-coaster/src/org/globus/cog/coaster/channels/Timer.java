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
 * Created on Jan 16, 2011
 */
package org.globus.cog.coaster.channels;

import java.util.TimerTask;

public class Timer {
	private static final java.util.Timer TIMER = new java.util.Timer();
	
	public static void every(final long interval, final Runnable action) {
		TIMER.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				action.run();
			}			
		}, interval, interval);
	}
	
	public static void every(final long interval, final TimerTask task) {
		TIMER.scheduleAtFixedRate(task, interval, interval);
	}

	public static void schedule(TimerTask task, long delay, long period) {
		TIMER.schedule(task, delay, period);
	}

	public static void schedule(TimerTask task, long delay) {
		TIMER.schedule(task, delay);
	}
}
