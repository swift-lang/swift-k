/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.Map;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.restartLog.LogEntry;
import org.globus.cog.karajan.workflow.nodes.restartLog.MutableInteger;

public class IsLogged extends VDLFunction {
	static {
		setArguments(IsLogged.class, new Arg[] { PA_VAR, PA_PATH });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		String fileName = getFileName(stack);
		Map map = getLogData(stack);
		LogEntry entry = LogEntry.build(fileName);
		boolean found = false;
		synchronized (map) {
			MutableInteger count = (MutableInteger) map.get(entry);
			if (count != null && count.getValue() > 0) {
				count.dec();
				found = true;
			}
		}
		return Boolean.valueOf(found);
	}
}
