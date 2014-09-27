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

import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.mapping.DSHandle;

public class PartialCloseDataset extends SwiftFunction {
	public static final Logger logger = Logger.getLogger(CloseDataset.class);
	
	private ArgRef<DSHandle> var;
	private ArgRef<Number> count;
	
	@Override
    protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
            throws CompilationException {
		if (var.isStatic()) {
			if (var.getValue().isClosed()) {
				// variables assigned at compile-time are already closed, so optimize this away
				return null;
			}
		}
        return super.compileBody(w, argScope, scope);
    }

    @Override
    protected Signature getSignature() {
        return new Signature(params("var", optional("count", 1)));
    }

	@Override
	public Object function(Stack stack) {
		DSHandle var = this.var.getValue(stack);
		if (logger.isDebugEnabled()) {
			logger.debug("Partially closing " + var);
		}
		
		int count = this.count.getValue(stack).intValue();
		var.updateWriteRefCount(-count);
		return null;
	}
}
