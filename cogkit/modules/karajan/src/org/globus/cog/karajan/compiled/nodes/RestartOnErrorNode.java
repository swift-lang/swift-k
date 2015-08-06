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
 * Created on Jul 23, 2003
 */
package org.globus.cog.karajan.compiled.nodes;

import java.util.LinkedList;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.DynamicScope;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.parser.WrapperNode;

public class RestartOnErrorNode extends AbstractRegexpFailureHandler {
	private ArgRef<String> match;
	private ArgRef<Number> times;
	private String counter;
	private Node body;
	
	private VarRef<Object> var;
	
	@Override
	protected Signature getSignature() {
		return new Signature(params(optional("match", null), identifier("counter"), "times", block("body")));
	}


	@Override
	protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
			throws CompilationException {
		if (times.getValue() != null && times.getValue().intValue() == 0) {
			return body;
		}
		else {
			return super.compileBody(w, argScope, scope);
		}
	}
	
	@Override
    protected void compileBlocks(WrapperNode w, Signature sig, LinkedList<WrapperNode> blocks,
            Scope scope) throws CompilationException {
        Var v = scope.addVar(counter);
        var = scope.getVarRef(v);
        DynamicScope ds = new DynamicScope(w, scope);
        super.compileBlocks(w, sig, blocks, ds);
        ds.close();
    }

	protected void runBody(LWThread thr) {
        int i = thr.checkSliceAndPopState();
        int fc = thr.popIntState();
        int times = thr.popIntState();
        Stack stack = thr.getStack();
        int ec = childCount();
        if (i == 0) {
            fc = stack.frameCount();
            times = this.times.getValue(stack).intValue();
            i++;
            var.setValue(stack, i);
        }
        while (true) {
	        try {
	        	if (CompilerSettings.PERFORMANCE_COUNTERS) {
					startCount++;
				}
	            body.run(thr);
	            break;
	        }
	        catch (Yield y) {
	        	y.getState().push(times);
	        	y.getState().push(fc);
	            y.getState().push(i);
	            throw y;
	        }
	        catch (ExecutionException e) {
	        	stack.dropToFrame(fc);
	        	String match = this.match.getValue(stack);
	            if (match == null || matches(match, e)) {
	                times--;
	            }
	            if (times < 0) {
	                throw e;
	            }
	            i++;
	            var.setValue(stack, i);
	        }
        }
    }
}
