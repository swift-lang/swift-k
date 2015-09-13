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

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.griphyn.vdl.karajan.SwiftContext;
import org.griphyn.vdl.karajan.lib.restartLog.LogEntry;
import org.griphyn.vdl.karajan.lib.restartLog.RestartLogData;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.type.Types;

public class IsLogged extends SwiftFunction {
	private ArgRef<DSHandle> var;
	private VarRef<SwiftContext> context;
	
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
		RestartLogData log = context.getValue(stack).getRestartLog();
		return Boolean.valueOf(isLogged(log, var));
	}
	
	public static boolean isLogged(RestartLogData logData, DSHandle var) {

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

    public static boolean isLogged(RestartLogData logData, String str) {
        LogEntry entry = LogEntry.build(str);
        return logData.contains(entry);
    }
}
