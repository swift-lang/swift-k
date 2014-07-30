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
 * Created on Jul 19, 2010
 */
package org.globus.cog.karajan.compiled.nodes.restartLog;

import java.io.IOException;

import org.apache.log4j.Logger;

public class SyncThread extends Thread {
	public static final Logger logger = Logger.getLogger(SyncThread.class);

	private FlushableLockedFileWriter writer;
	private volatile boolean flushing;

	public SyncThread(FlushableLockedFileWriter writer) {
		super("Restart Log Sync");
		setDaemon(true);
		this.writer = writer;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				doFlush();
			}
		});
	}

	public void flush() {
		if (!flushing) {
			synchronized(this) {
				this.notifyAll();
			}
		}
	}

	public void run() {
		try {
			while (true) {
				synchronized (this) {
					flushing = false;
					wait();
					flushing = true;
				}
				doFlush();
			}
		}
		catch (InterruptedException e) {
		}
	}

	private void doFlush() {
		try {
			writer.actualFlush();
		}
		catch (IOException e) {
			logger.warn("Failed to sync restart log", e);
		}
	}
}
