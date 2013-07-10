//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 22, 2006
 */
package org.globus.cog.karajan.compiled.nodes.restartLog;

import java.io.File;
import java.io.IOException;

import k.rt.ChannelOperator;

public class LogChannelOperator extends ChannelOperator<String, String> {
	private final FlushableLockedFileWriter writer;
	private boolean closed;

	public LogChannelOperator(FlushableLockedFileWriter writer) {
		super(null);
		this.writer = writer;
	}

	protected Object initialValue() {
		return null;
	}

	protected synchronized String update(String oldvalue, String str) {
		if (closed) {
			return null;
		}
		try {
			writer.write(str);
			if (!str.endsWith("\n")) {
				writer.write('\n');
			}
			writer.flush();
		}
		catch (IOException e) {
			throw new RuntimeException("Exception caught while writing to log file", e);
		}
		return null;
	}

	public boolean isCommutative() {
		return true;
	}

	public synchronized void close() {
		if (!closed) {
			try {
				writer.close();
			}
			catch (IOException e) {
				throw new RuntimeException("Failed to close restart log", e);
			}
			closed = true;
		}
	}

	public File getFile() {
		return writer.getFile();
	}
}