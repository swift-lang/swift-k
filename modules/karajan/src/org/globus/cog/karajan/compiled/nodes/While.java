/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 2, 2004
 */
package org.globus.cog.karajan.compiled.nodes;

import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;

import k.rt.SingleValueChannel;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.DynamicScope;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.parser.WrapperNode;

public class While extends InternalFunction {
	private String name;
	private ArgRef<Object> initial;
	
	private Node body;
	private ChannelRef.DynamicSingleValued<Object> c_next;
	
	private VarRef<Object> var;
	
	@Override
	protected Signature getSignature() {
		return new Signature(params(identifier("name"), "initial", block("body")));
	}

	@Override
	protected void compileBlocks(WrapperNode w, Signature sig, LinkedList<WrapperNode> blocks,
			Scope scope) throws CompilationException {
		Var v = scope.addVar(name);
		var = scope.getVarRef(v);
		Var.Channel next = scope.addChannel("next");
		c_next = new ChannelRef.DynamicSingleValued<Object>("next", next.getIndex());
		DynamicScope ds = new DynamicScope(w, scope);
		super.compileBlocks(w, sig, blocks, ds);
		ds.close();
	}

	@Override
	public void dump(PrintStream ps, int level) throws IOException {
		super.dump(ps, level);
		if (body != null) {
			body.dump(ps, level + 1);
		}
	}
	
	@Override
	public void runBody(LWThread thr) {
		if (body == null) {
			return;
		}
	    int i = thr.checkSliceAndPopState();
	    @SuppressWarnings("unchecked")
		SingleValueChannel<Object> next = (SingleValueChannel<Object>) thr.popState();
	    Stack stack = thr.getStack();
	    try {
	    	switch(i) {
	    		case 0:
	    			var.setValue(stack, initial.getValue(stack));
	    			c_next.create(stack);
	    			next = (SingleValueChannel<Object>) c_next.get(stack);
	    			i++;
	    		case 1:
	    			while (true) {
	    				body.run(thr);
	    				if (next.isEmpty()) {
	    					break;
	    				}
	    				Object val = next.removeFirst();
	    				var.setValue(stack, val);
	    			}
	    	}
	    }
	    catch (Yield y) {
	    	y.getState().push(next);
	    	y.getState().push(i);
	    	throw y;
	    }
	}
}