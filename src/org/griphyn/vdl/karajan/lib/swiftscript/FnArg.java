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
 * Created on Sep 28, 2006
 */
package org.griphyn.vdl.karajan.lib.swiftscript;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import k.rt.Channel;
import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.analyzer.VariableNotFoundException;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Field;


public class FnArg extends SwiftFunction {
    private ChannelRef<AbstractDataNode> c_vargs;
    
    private VarRef<Map<String, String>> parsedArgs;
    
	@Override
    protected Signature getSignature() {
        return new Signature(params("..."));
    }
	
	@Override
    protected Field getReturnType() {
        return Field.GENERIC_STRING;
    }

    @Override
    protected void addLocals(Scope scope) {
        super.addLocals(scope);
        try {
            parsedArgs = scope.getVarRef("SWIFT:PARSED_ARGS");
        }
        catch (VariableNotFoundException e) {
            VarRef<Context> context = scope.getVarRef("#context");
            scope.getRoot().addVar("SWIFT:PARSED_ARGS", parseArgs(context.getValue().getArguments()));
            parsedArgs = scope.getVarRef("SWIFT:PARSED_ARGS");
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Object function(Stack stack) {
		Map<String, String> args = this.parsedArgs.getValue(stack);
		Channel<AbstractDataNode> fargs = c_vargs.get(stack);
		
		if (fargs.size() < 1) {
		    throw new ExecutionException(this, "Missing argument 'name'");
		}
		
        AbstractDataNode hname = (AbstractDataNode) fargs.get(0);
		hname.waitFor(this);
		AbstractDataNode hvalue = null;
		if (fargs.size() == 2) {
		    hvalue = (AbstractDataNode) fargs.get(1);
		}
		if (hvalue != null) {
		    hvalue.waitFor(this);
		}
		String name = (String) hname.getValue();
		name = name.trim();
		if (name.startsWith("\"") && name.endsWith("\"")) {
			name = name.substring(1, name.length() - 1);
		} 
		Object value = args.get(name);
		if (value == null && hvalue != null) {
			value = hvalue.getValue();
		}
		if (value == null) {
			throw new ExecutionException("Missing command line argument: " + name);
		}
		else {
			DSHandle result = NodeFactory.newRoot(Field.GENERIC_STRING, value);
			if (PROVENANCE_ENABLED) {
			    int provid = nextProvenanceID();
			    logProvenanceResult(provid, result, "arg");
			    logProvenanceParameter(provid, hname, "name");
			    if (hvalue != null) {
			        logProvenanceParameter(provid, hvalue, "value");
			    }
			}
			return result;
		}
	}
    
    public static Map<String, String> parseArgs(List<String> argv) {
        Map<String, String> named = new HashMap<String, String>();
        for (String arg : argv) {
            if (!arg.startsWith("-")) {
                continue;
            }
            int index = arg.indexOf('=');
            if (index == -1 || (arg.charAt(0) != '-')) {
                throw new ExecutionException("Invalid command line argument: " + arg);
            }
            else {
                String name = arg.substring(1, index);
                named.put(name, arg.substring(index + 1));
            }
        }
        return named;
    }
}
