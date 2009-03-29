/*
 * Created on Aug 28, 2008
 */
package org.griphyn.vdl.karajan.monitor.processors;

import org.apache.log4j.Level;
import org.globus.cog.karajan.stack.VariableStack;
import org.griphyn.vdl.karajan.VDL2ExecutionContext;
import org.griphyn.vdl.karajan.monitor.SystemState;

public class ExecutionContextProcessor implements LogMessageProcessor {

	public Object getSupportedCategory() {
		return Level.INFO;
	}

	public String getSupportedSource() {
		return VDL2ExecutionContext.class.getName();
	}

	public void processMessage(SystemState state, Object message, Object details) {
		if (message instanceof VariableStack) {
		    state.setStack((VariableStack) message);
		}
	}
}
