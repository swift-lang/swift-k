//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 22, 2006
 */
package org.globus.cog.karajan.workflow.nodes.restartLog;

import java.io.File;
import java.io.IOException;

import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.nodes.functions.VariableArgumentsOperator;

public class LogVargOperator extends VariableArgumentsOperator {
	private final FlushableLockedFileWriter writer;
	private boolean closed;

	public LogVargOperator(FlushableLockedFileWriter writer) {
		this.writer = writer;
	}

	protected Object initialValue() {
		return null;
	}

	protected synchronized Object update(Object oldvalue, Object item) {
		try {
			String str = (String) item;
			writer.write(str);
			if (!str.endsWith("\n")) {
				writer.write('\n');
			}
			writer.flush();
		}
		catch (IOException e) {
			throw new KarajanRuntimeException("Exception caught while writing to log file", e);
		}
		return null;
	}

	public boolean isCommutative() {
		return true;
	}

	public synchronized void close() throws IOException {
		if (!closed) {
			writer.close();
			closed = true;
		}
	}

	public File getFile() {
		return writer.getFile();
	}
}