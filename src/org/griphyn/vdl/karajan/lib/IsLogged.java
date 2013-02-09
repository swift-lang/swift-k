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
import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.restartLog.LogEntry;
import org.globus.cog.karajan.compiled.nodes.restartLog.RestartLog;
import org.globus.cog.karajan.util.TypeUtil;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.Path;

public class IsLogged extends SwiftFunction {
	private ArgRef<DSHandle> var;
	private ArgRef<Object> path;
	
	private VarRef<Context> context;
	
	@Override
    protected Signature getSignature() {
        return new Signature(params("var", "path"));
    }
	
	@Override
    protected void addLocals(Scope scope) {
        context = scope.getVarRef("#context");
        super.addLocals(scope);
    }

	@Override
    public Object function(Stack stack) {	
		DSHandle var = this.var.getValue(stack);
		Path path;
		Object p = this.path.getValue(stack);
		if (p instanceof Path) {
			path = (Path) p;
		}
		else {
			path = Path.parse(TypeUtil.toString(p));
		}
		return Boolean.valueOf(isLogged(context.getValue(stack), var, path));
	}
	
	public static boolean isLogged(Context ctx, DSHandle var, Path path) throws ExecutionException {
		@SuppressWarnings("unchecked")
        Map<LogEntry, Object> logData = (Map<LogEntry, Object>) ctx.getAttribute(RestartLog.LOG_DATA);
	    path = var.getPathFromRoot().append(path);
        LogEntry entry = LogEntry.build(var.getRoot().getParam(MappingParam.SWIFT_RESTARTID) + "." + path.stringForm());
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
