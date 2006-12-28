//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 21, 2006
 */
package org.globus.cog.karajan.scheduler.submitQueue;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.util.KarajanProperties;

public class GlobalSubmitQueue extends AbstractSubmitQueue {
	public static final Logger logger = Logger.getLogger(GlobalSubmitQueue.class);

	public static final int DEFAULT_GLOBAL_SUBMIT_THROTTLE = 100;

	private static GlobalSubmitQueue queue;

	public synchronized static GlobalSubmitQueue getQueue() {
		if (queue == null) {
			queue = new GlobalSubmitQueue();
		}
		return queue;
	}

	public GlobalSubmitQueue() {
		super(DEFAULT_GLOBAL_SUBMIT_THROTTLE);
		String globalSubmitThrottle = KarajanProperties.getDefault().getProperty(
				"global.submit.throttle");
		if (globalSubmitThrottle != null) {
			try {
				int gst = Integer.parseInt(globalSubmitThrottle);
				setThrottle(gst);
			}
			catch (Exception e) {
				logger.warn("Failed to set global submit throttle", e);
			}
		}
		else {
			logger.info("No global submit throttle set. Using default ("
					+ DEFAULT_GLOBAL_SUBMIT_THROTTLE + ")");
		}
	}
}
