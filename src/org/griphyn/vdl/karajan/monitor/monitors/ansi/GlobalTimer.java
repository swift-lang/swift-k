/*
 * Created on Sep 23, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import java.util.Timer;

public class GlobalTimer {
	private static final Timer timer = new Timer();

	public static Timer getTimer() {
		return timer;
	}
}
