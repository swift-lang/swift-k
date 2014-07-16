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
