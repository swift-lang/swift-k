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


package org.griphyn.vdl.karajan.lib;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.DSHandle;

public class SetWaitCount extends SwiftFunction {
	public static final Logger logger = Logger.getLogger(CloseDataset.class);

	private ArgRef<DSHandle> var;
    private ArgRef<Number> count;

    @Override
    protected Signature getSignature() {
        return new Signature(params("var", optional("count", 1)));
    }

    @Override
	public Object function(Stack stack) {
		DSHandle var = this.var.getValue(stack);

		if (var.isClosed()) {
		    // Static mappers will close the array sizes during initialization.
		    // Such an array passed to a function assigning to elements
		    // of that array in a loop will attempt to increase the 
		    // wait count. That is a legit situation to have.
		    if (var.getMapper().isStatic()) {
		        // ignore
		    }
		    else {
		        throw new ExecutionException("Attempted to set a wait count for a closed variable " + var);
		    }
		}
		
		int count = this.count.getValue(stack).intValue();
		var.setWriteRefCount(count);
		return null;
	}
}
