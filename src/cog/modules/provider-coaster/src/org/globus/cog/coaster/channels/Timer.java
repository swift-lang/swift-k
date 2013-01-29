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
