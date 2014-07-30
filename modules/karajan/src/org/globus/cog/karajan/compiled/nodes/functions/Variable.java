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
package org.globus.cog.karajan.compiled.nodes.functions;

import k.rt.Stack;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;

public class Variable extends Node {
	final static Logger logger = Logger.getLogger(Variable.class);

	private VarRef<Object> ref;
	private ChannelRef<Object> _vargs;

	@Override
	public void run(LWThread thr) {
		Stack stack = thr.getStack();
		_vargs.append(stack, ref.getValue(stack));
	}

	@Override
	public Node compile(WrapperNode wn, Scope scope) throws CompilationException {
		super.compile(wn, scope);
		String name = wn.getText();
		
		ref = scope.getVarRef(name);
		Var.Channel vargs = scope.lookupChannel(Param.VARGS);
		
		if (ref.isStatic()) {
			if (vargs.append(ref.getValue())) {
				return null;
			}
			else {
				_vargs = scope.getChannelRef(vargs);
				return this;
			}
		}
		else {
			vargs.appendDynamic();
			_vargs = scope.getChannelRef(vargs);
			return this;
		}
	}
}
