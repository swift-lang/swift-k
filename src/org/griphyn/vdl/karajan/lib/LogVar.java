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
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.analyzer.VariableNotFoundException;
import org.griphyn.vdl.karajan.SwiftContext;
import org.griphyn.vdl.karajan.lib.restartLog.RestartLogData;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.type.Types;

public class LogVar extends SwiftFunction {
    private ArgRef<DSHandle> var;
    private VarRef<SwiftContext> context;
    
	@Override
    protected Signature getSignature() {
        return new Signature(params("var"), returns(channel("restartlog", 1)));
    }
	
	@Override
    protected void addLocals(Scope scope) {
        context = scope.getVarRef("#context");
        super.addLocals(scope);
    }

    @Override
	public Object function(Stack stack) {
		DSHandle var = this.var.getValue(stack);
        logVar(context.getValue(stack).getRestartLog(), var);
		return null;
	}
		
	public static void logVar(RestartLogData log, DSHandle var) throws VariableNotFoundException {
	    if (var.getType().equals(Types.EXTERNAL)) {
	        log.add(getLogId(var));
	    }
	    else {
	        PhysicalFormat pf = var.map();
            if (pf == null) {
                throw new IllegalArgumentException(var + " could not be mapped");
            } 
            else {
                log.add(pf.toString());
            }
	    }
    }
		
	public static String getLogId(DSHandle var) {
        RootHandle root = var.getRoot();
        LWThread thr = root.getThread();
        return thr.getQualifiedName() + ":" + root.getName() + 
            "." + var.getPathFromRoot().stringForm();
    }
}
