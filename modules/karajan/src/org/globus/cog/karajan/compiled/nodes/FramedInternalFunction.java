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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 19, 2012
 */
package org.globus.cog.karajan.compiled.nodes;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.ContainerScope;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.parser.WrapperNode;


public abstract class FramedInternalFunction extends InternalFunction {
	private int varCount;

	public Node compile(WrapperNode w, Scope scope) throws CompilationException {
		ContainerScope cs = new ContainerScope(w, scope);
		Integer l = (Integer) WrapperNode.getTreeProperty(WrapperNode.LINE, w);
		if (l != null) {
			setLine(l);
		}
		setType(w.getNodeType());
		Node fn = compileChildren(w, cs);
		varCount = cs.size();
		return fn;
	}
	
	protected void setVarCount(int count) {
		this.varCount = count;
	}
	
	protected void enter(Stack stack) {
		stack.enter(this, varCount);
	}
	
	protected void leave(Stack stack) {
		stack.leave();
	}
	
	@Override
	public void run(LWThread thr) {
		int ec = childCount();
        int i = thr.checkSliceAndPopState();
        Stack stack = thr.getStack();
        try {
	        switch (i) {
	        	case 0:
	        		stack.enter(this, varCount);
	        		initializeArgs(stack);
	        		i++;
	        	default:
			            for (; i <= ec; i++) {
			            	runChild(i - 1, thr);
			            }
			            i = Integer.MAX_VALUE;
	        	case Integer.MAX_VALUE:
			            try {
			            	runBody(thr);
			            	stack.leave();
			            }
			            catch (RuntimeException e) {
			            	throw new ExecutionException(this, e);
			            }
		    }
        }
        catch (Yield y) {
            y.getState().push(i);
            throw y;
        }
	}
}
