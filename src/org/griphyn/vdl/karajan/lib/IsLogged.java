/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.List;
import java.util.Map;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.restartLog.LogEntry;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;

public class IsLogged extends VDLFunction {
	static {
		setArguments(IsLogged.class, new Arg[] { PA_VAR, PA_PATH });
	}

	public Object function(VariableStack stack) throws ExecutionException {		
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		Path path;
		Object p = PA_PATH.getValue(stack);
		if (p instanceof Path) {
			path = (Path) p;
		}
		else {
			path = Path.parse(TypeUtil.toString(p));
		}
		return Boolean.valueOf(isLogged(stack, var, path));
	}
	
	public static boolean isLogged(VariableStack stack, DSHandle var, Path path) throws ExecutionException {
	    path = var.getPathFromRoot().append(path);
        LogEntry entry = LogEntry.build(var.getRoot().getParam("swift#restartid") + "." + path.stringForm());
        Map map = getLogData(stack);
        boolean found = false;
        synchronized (map) {
            List files = (List) map.get(entry);
            if (files != null && !files.isEmpty()) {
                found = true;
            }
        }
        return found;
	}
}
