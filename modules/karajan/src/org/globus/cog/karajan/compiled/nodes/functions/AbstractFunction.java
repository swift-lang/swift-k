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
 * Created on Jul 8, 2003
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.parser.WrapperNode;

public abstract class AbstractFunction extends InternalFunction {
	
	protected ChannelRef<Object> cr_vargs;
	
	@Override
	protected ArgInfo compileArgs(WrapperNode w, Signature sig, Scope scope)
			throws CompilationException {
		Var.Channel cr = scope.lookupChannel("...");
		cr_vargs = scope.getChannelRef(cr);
		return super.compileArgs(w, sig, scope);
	}

	@Override
	public void runBody(LWThread thr) {
		Stack stack = thr.getStack();
		ret(stack, function(stack));
	}

	protected void ret(Stack stack, final Object value) {
		if (value != null) {
			if (value.getClass().isArray()) {
				try {
					Object[] array = (Object[]) value;
					for (int i = 0; i < array.length; i++) {
						cr_vargs.append(stack, array[i]);
					}
				}
				catch (ClassCastException e) {
					// array of primitives; return as is
					cr_vargs.append(stack, value);
				}
			}
			else {
				cr_vargs.append(stack, value);
			}
		}
	}
	
	protected boolean staticReturn(Scope scope, Object value) {
		Var.Channel crv = scope.parent.lookupChannel("...");
		return crv.append(value);
	}
	
	protected void returnDynamic(Scope scope) {
		Var.Channel crv = scope.parent.lookupChannel("...");
		crv.appendDynamic();
	}
	
	public abstract Object function(Stack stack);
}