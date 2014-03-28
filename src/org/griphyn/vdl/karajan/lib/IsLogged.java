/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.List;
import java.util.Map;

import k.rt.Context;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.restartLog.LogEntry;
import org.globus.cog.karajan.compiled.nodes.restartLog.RestartLog;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.type.Types;

public class IsLogged extends SwiftFunction {
	private ArgRef<DSHandle> var;
	
	private VarRef<Context> context;
	
	@Override
    protected Signature getSignature() {
        return new Signature(params("var"));
    }
	
	@Override
    protected void addLocals(Scope scope) {
        context = scope.getVarRef("#context");
        super.addLocals(scope);
    }

	@Override
    public Object function(Stack stack) {	
		DSHandle var = this.var.getValue(stack);
		return Boolean.valueOf(isLogged(context.getValue(stack), var));
	}
	
	public static boolean isLogged(Context ctx, DSHandle var) {
		@SuppressWarnings("unchecked")
        Map<LogEntry, Object> logData = (Map<LogEntry, Object>) ctx.getAttribute(RestartLog.LOG_DATA);
		if (logData.isEmpty()) {
		    return false;
		}
		
		/*
		 * One cannot use the file name to check if external data is logged
		 * since there is no explicit file name known to swift for external
		 * data.
		 */
		if (var.getType().equals(Types.EXTERNAL)) {
		    return isLogged(logData, LogVar.getLogId(var));
        }
        else {
            PhysicalFormat pf = var.map();
            if (pf == null) {
                throw new IllegalArgumentException(var + " could not be mapped");
            } 
            else {
                return isLogged(logData, pf.toString());
            }
        }
	}

    private static boolean isLogged(Map<LogEntry, Object> logData, String str) {
        LogEntry entry = LogEntry.build(str);
        boolean found = false;
        synchronized (logData) {
            List<?> files = (List<?>) logData.get(entry);
            if (files != null && !files.isEmpty()) {
                found = true;
            }
        }
        return found;
    }
}
