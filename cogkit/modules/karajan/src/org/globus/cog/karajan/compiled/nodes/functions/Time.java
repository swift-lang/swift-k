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
 * Created on Feb 19, 2005
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;

public class Time extends InternalFunction {
	public static final String START = "#start";
	
	private ChannelRef<Object> cr_vargs;
	private ChannelRef<Object> c_vargs;
	
	private VarRef<Long> start;
	
	@Override
	protected Signature getSignature() {
		return new Signature(params("..."), returns(channel("...", 1)));
	}
	
	@Override
	protected void addLocals(Scope scope) {
		super.addLocals(scope);
		start = scope.getVarRef(scope.addVar("#start"));
	}

	@Override
	public void run(LWThread thr) {
	    Stack stack = thr.getStack();
	    if (start.getValue(stack) == null) {
	        start.setValue(stack, System.currentTimeMillis());
	    }
		super.run(thr);
		cr_vargs.append(stack, System.currentTimeMillis() - start.getValue(stack));
	}
}