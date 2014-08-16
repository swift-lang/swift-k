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

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.karajan.FileNameExpander;
import org.griphyn.vdl.karajan.FileNameExpander.MultiMode;
import org.griphyn.vdl.karajan.FileNameExpander.Transform;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Field;

public class FileNames extends SwiftFunction {
	private ArgRef<AbstractDataNode> var;
	private boolean inAppInvocation;
	
	@Override
    public Node compile(WrapperNode w, Scope scope) throws CompilationException {        
        Node self = super.compile(w, scope);
        // either execute(arguments(this)) or execute(named(stdxxx, this)) 
        if (getParent().getParent().getType().equals("swift:execute")) {
            inAppInvocation = true;
        }
        return self;
    }

    @Override
    protected Signature getSignature() {
        return new Signature(params("var"));
    }

    @Override
	public Object function(Stack stack) {
        AbstractDataNode var = this.var.getValue(stack);
	
        DSHandle result;
        
        if (inAppInvocation) {
            result = NodeFactory.newRoot(Field.GENERIC_ANY, 
                new FileNameExpander(var, MultiMode.SEPARATE, Transform.RELATIVE));
        }
        else {
            result = NodeFactory.newRoot(Field.GENERIC_STRING, 
                new FileNameExpander(var, MultiMode.COMBINED, Transform.NONE).toCombinedString());
        }
		// DSHandle returnArray = NodeFactory.newRoot(Field.GENERIC_STRING_ARRAY, Arrays.asList(f));
		// returnArray.closeShallow();
		
		if (PROVENANCE_ENABLED) {
		    int provid = SwiftFunction.nextProvenanceID();
		    logProvenanceParameter(provid, var, "input");
		    logProvenanceResult(provid, result, "filenames");
		}

		return result;
	}
}
