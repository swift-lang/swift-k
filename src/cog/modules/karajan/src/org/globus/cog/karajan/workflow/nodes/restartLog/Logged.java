//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 22, 2006
 */
package org.globus.cog.karajan.workflow.nodes.restartLog;

import java.util.Map;

import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.Sequential;

public class Logged extends Sequential {
	protected void post(VariableStack stack) throws ExecutionException {
		RestartLog.LOG_CHANNEL.ret(stack, LogEntry.build(stack, this).toString());
		super.post(stack);
	}

	protected void executeChildren(VariableStack stack) throws ExecutionException {
		try {
			Map map = (Map) stack.getDeepVar(RestartLog.LOG_DATA);
			LogEntry entry = LogEntry.build(stack, this);
			boolean found = false;
			synchronized(map) {
				MutableInteger count = (MutableInteger) map.get(entry);
				if (count != null && count.getValue() > 0) {
					count.dec();
					found = true;
				}
			}
			if (found) {
				complete(stack);
			}
			else {
				super.executeChildren(stack);
			}
		}
		catch (VariableNotFoundException e) {
			throw new ExecutionException("No restart log environment found");
		}
	}
}
