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

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.OOBYield;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;

public class GetFieldSubscript extends SwiftFunction {
    private ArgRef<DSHandle> var;
    private ArgRef<AbstractDataNode> subscript;

	@Override
    protected Signature getSignature() {
        return new Signature(params("var", "subscript"));
    }

	@Override
	public Object function(Stack stack) {
		DSHandle var = this.var.getValue(stack);

		AbstractDataNode indexh = this.subscript.getValue(stack);

		try {
		    indexh.waitFor();
		    Object index = indexh.getValue();
			if ("*".equals(index)) {
			    return var.getAllFields();
			}
			else {
			    return var.getField(Path.EMPTY_PATH.addFirst((Comparable<?>) index, true));
			}
		}
		catch (OOBYield y) {
		    throw y.wrapped(this);
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(this, e);
		}
		catch (HandleOpenException e) {
			throw new ExecutionException(this, e);
		}
	}


}
