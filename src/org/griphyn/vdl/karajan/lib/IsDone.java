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
 * Created on Jul 18, 2010
 */
package org.griphyn.vdl.karajan.lib;

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.griphyn.vdl.karajan.SwiftContext;
import org.griphyn.vdl.karajan.lib.restartLog.RestartLogData;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.type.Types;

public class IsDone extends SwiftFunction {
    private ArgRef<Iterable<DSHandle>> stageout;
    private VarRef<SwiftContext> context;

    @Override
    protected Signature getSignature() {
        return new Signature(params("stageout"), returns(channel("...", 1)));
    }
    
    @Override
    protected void addLocals(Scope scope) {
        context = scope.getVarRef("#context");
        super.addLocals(scope);
    }
    
    @Override
    public Object function(Stack stack) {
    	RestartLogData log = context.getValue(stack).getRestartLog();
        Iterable<DSHandle> files = stageout.getValue(stack);
        for (DSHandle file : files) {
        	if (file.isRestartable()) {
        	    if (file.getType().equals(Types.EXTERNAL)) {
        	        return IsLogged.isLogged(log, LogVar.getLogId(file));
        	    }
        	    else if (file.getMapper().isStatic()) {                
                    try {
                        for (DSHandle leaf : file.getLeaves()) {
                            if (!IsLogged.isLogged(log, leaf)) {
                                return Boolean.FALSE;
                            }
                        }
                    }
                    catch (HandleOpenException e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Handle open caught in isDone for " + file + ". Assuming false.");
                        }
                        return Boolean.FALSE;
                    }
        		}
        		else {
        			/*
        			 * Checking if data with a dynamic mapper is done cannot involve
        			 * iterating over the data items, since the items are not known
        			 * until after the app runs. In such cases revert to logging
        			 * and checking the entire variable rather than its items
        			 */
        			
        			if (!IsLogged.isLogged(log, file)) {
        				return Boolean.FALSE;
        			}
        		}
            }
        	else {
        	    return Boolean.FALSE;
        	}
        }
        if (!files.iterator().hasNext()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
