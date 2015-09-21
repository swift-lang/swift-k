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


package org.griphyn.vdl.karajan.lib.swiftscript;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.karajan.FileNameResolver;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Field;


public class ExtractFloat extends SwiftFunction {
    private ArgRef<AbstractDataNode> var;

    @Override
    protected Signature getSignature() {
        return new Signature(params("var"));
    }

    @Override
	public Object function(Stack stack) {
		AbstractDataNode handle = this.var.getValue(stack);
		try {
			handle.waitFor(this);
			
			String fn = new FileNameResolver(handle).getSingleLocalPath();
			
			Reader freader = new FileReader(fn);
			BufferedReader breader = new BufferedReader(freader);
			String str = breader.readLine();
			freader.close();
			DSHandle result = NodeFactory.newRoot(Field.GENERIC_FLOAT, Double.parseDouble(str));
			int provid = SwiftFunction.nextProvenanceID();
			SwiftFunction.logProvenanceResult(provid, result, "extractfloat");
			SwiftFunction.logProvenanceParameter(provid, handle, "filename");
			return result;
		}
		catch (DependentException e) {
            return NodeFactory.newRoot(Field.GENERIC_FLOAT, e);
        }
		catch (IOException ioe) {
			throw new ExecutionException("Reading integer content of file", ioe);
		}
	}
}
