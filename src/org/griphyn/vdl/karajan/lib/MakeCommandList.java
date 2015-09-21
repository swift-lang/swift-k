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

import java.util.Collection;

import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.karajan.Command;

public class MakeCommandList extends SwiftFunction {
	public static final Logger logger = Logger.getLogger(MakeCommandList.class);
		
	private ChannelRef<Command> c_vargs;
	private int size;
	
	@Override
    protected Signature getSignature() {
        return new Signature(params("..."));
    }

	@Override
    protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
            throws CompilationException {
	    size = w.nodeCount();
        return super.compileBody(w, argScope, scope);
    }

    @Override
	public Object function(Stack stack) {
        Command[] commands = new Command[size];
        
        Collection<Command> l = c_vargs.get(stack);
        int index = 0;
        for (Command cmd : l) {
            commands[index++] = cmd;
        }
        
        return commands;
	}
    
    protected void ret(Stack stack, final Object value) {
        cr_vargs.append(stack, value);
    }
}
