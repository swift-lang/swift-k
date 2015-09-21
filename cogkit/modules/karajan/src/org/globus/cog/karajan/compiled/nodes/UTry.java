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
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 6, 2004
 */
package org.globus.cog.karajan.compiled.nodes;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.parser.WrapperNode;

public class UTry extends CompoundNode {	
	public static final String EXCEPTION_VAR_NAME = "#exception";
	private VarRef<ExecutionException> exception;	

	@Override
	protected Node compileChildren(WrapperNode w, Scope scope) throws CompilationException {
		Var v = scope.addVar(EXCEPTION_VAR_NAME);
		exception = scope.getVarRef(v);
		Var.Channel r = scope.parent.lookupChannel("...");
		r.appendDynamic();
		Node fn = super.compileChildren(w, scope);
		return fn;
	}

	public void run(LWThread thr) {
		int ec = childCount();
        int i = thr.checkSliceAndPopState(ec + 2);
        int fc = thr.popIntState();
        Stack stack = thr.getStack();
        try {
        	switch (i) {
        		case 0:
        			fc = stack.frameCount();
        			i++;
        		default:
        			for (; i <= ec; i++) {
        				try {
        					runChild(i - 1, thr);
       						exception.setValue(stack, null);
        					break;
        				}
        				catch (ExecutionException e) {
        					stack.dropToFrame(fc);
        					exception.setValue(stack, e);
        				}
        			}
        			i = Integer.MAX_VALUE;
        		case Integer.MAX_VALUE:
        			ExecutionException lastEx = exception.getValue(stack);
        			if (lastEx != null) {
        				throw lastEx;
        			}
        	}
        }
        catch (Yield y) {
            y.getState().push(fc);
            y.getState().push(i, ec + 2);
            throw y;
        }
    }
}