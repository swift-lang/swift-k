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
 * Created on Jul 21, 2005
 */
package org.globus.cog.coaster.handlers;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.commands.TestCommand;

public class TestHandler extends RequestHandler {
	private static final Logger logger = Logger.getLogger(TestHandler.class);

	private static Timer timer = new Timer(true);

	private static TestCallback callback;

	public static void setCallback(TestCallback callback) {
		TestHandler.callback = callback;
	}
	
	public void requestComplete() throws ProtocolException {
		String mode = new String(getInData(0));
		if ("INITIAL".equals(mode)) {
			sendReply("OK".getBytes());
			final CoasterChannel channel = getChannel();
			timer.schedule(new TimerTask() {
				public void run() {
					TestCommand done = new TestCommand(false);
					CoasterChannel channel = null;
					try {
						done.execute(channel);
					}
					catch (Exception e) {
						logger.error("Got exception", e);
					}
				}
			}, 1000 * 5);
		}
		else {
			callback.done(getChannel());
			sendReply("OK".getBytes());
		}
	}

	public static interface TestCallback {
		void done(CoasterChannel channel);
	}
}
