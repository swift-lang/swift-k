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

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.futures.FutureFault;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;

public class GetFieldValue extends SwiftFunction {
	public static final Logger logger = Logger.getLogger(GetFieldValue.class);
	
	private ArgRef<AbstractDataNode> var;
    private ArgRef<Object> path; 
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("var", optional("path", Path.EMPTY_PATH)));
    }

	/**
	 * Takes a supplied variable and path, and returns the unique value at that
	 * path. Path can contain wildcards, in which case an array is returned.
	 */
    @Override
	public Object function(Stack stack) {	
		AbstractDataNode var = this.var.getValue(stack);

		try {
			Path path = parsePath(this.path.getValue(stack));
			if (path.hasWildcards()) {
			    // TODO we should clarify whether we allow generic selectors
			    return var.getField(path.butLast()).getAllFields().toArray();
			}
			else {
				var = (AbstractDataNode) var.getField(path);
				if (var.getType().isArray()) {
					throw new RuntimeException("Getting value for array " + var + " which is not permitted.");
				}
				var.waitFor(this);
				return var.getValue();
			}
		}
		catch (FutureFault f) {
		    throw f;
		}
		catch (Exception e) {
			throw new ExecutionException(this, e);
		}
	}
}
