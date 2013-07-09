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
package org.griphyn.vdl.karajan.lib.swiftscript;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootArrayDataNode;
import org.griphyn.vdl.type.Types;

public class FileNames extends SwiftFunction {
	private ArgRef<AbstractDataNode> var;

    @Override
    protected Signature getSignature() {
        return new Signature(params("var"));
    }

    @Override
	public Object function(Stack stack) {
        AbstractDataNode var = this.var.getValue(stack);
		String[] f = filename(var);
		DSHandle returnArray = new RootArrayDataNode(Types.STRING.arrayType());
		try {
			for (int i = 0; i < f.length; i++) {
				Path p = parsePath("["+i+"]");
				DSHandle h = returnArray.getField(p);
				h.setValue(relativize(f[i]));
			}
		} catch (InvalidPathException e) {
			throw new ExecutionException("Unexpected invalid path exception",e);
		}
		returnArray.closeShallow();
		
		if (PROVENANCE_ENABLED) {
		    int provid = SwiftFunction.nextProvenanceID();
		    logProvenanceParameter(provid, var, "input");
		    logProvenanceResult(provid, returnArray, "filenames");
		}

		return returnArray;
	}
}
