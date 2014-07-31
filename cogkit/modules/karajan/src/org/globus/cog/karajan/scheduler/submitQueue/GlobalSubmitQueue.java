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
 * Created on Jun 21, 2006
 */
package org.globus.cog.karajan.scheduler.submitQueue;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.util.KarajanProperties;

/**
 * A submit queue which enforces a global throttle on the number
 * of tasks being submitted. This is generally the first queue in the
 * set of submit queues and is shared across all scheduler instances
 * which generally honor throttling parameters.
 * 
 * @author Mihael Hategan
 *
 */
public class GlobalSubmitQueue extends AbstractSubmitQueue {
	public static final Logger logger = Logger.getLogger(GlobalSubmitQueue.class);

	public static final int DEFAULT_GLOBAL_SUBMIT_THROTTLE = 1024;

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
			if (logger.isInfoEnabled()) {
				logger.info("No global submit throttle set. Using default ("
					+ DEFAULT_GLOBAL_SUBMIT_THROTTLE + ")");
			}
		}
	}
}
