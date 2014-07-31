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

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Pure;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.parser.WrapperNode;

public class Named extends InternalFunction implements Pure {
	private String name;
	private ArgRef<Object> value;
	private VarRef<Object> ret;
	

	@Override
	protected Signature getSignature() {
		return new Signature(
			params(identifier("name"), "value")
		);
	}

	@Override
	public Node compileBody(WrapperNode w, Scope argScope, Scope scope) throws CompilationException {
		Var vret = scope.parent.lookupParam(name, this);
		if (!vret.isSettableByName()) {
			throw new CompilationException(this, "Cannot pass positional argument '" + name + "' by name");
		}
		if (value.isStatic()) {
			vret.setValue(value.getValue());
			
			if (childCount() == 0) {
				return null;
			}
			else {
				return this;
			}
		}
		else {
			vret.setDynamic();
			ret = scope.getVarRef(vret);
			return this;
		}
	}

	public void runBody(LWThread thr) {
		if (ret != null) {
			Stack stack = thr.getStack();
			ret.setValue(stack, value.getValue(stack));
		}
	}

	@Override
	public String getTextualName() {
		if (name == null) {
			return super.getTextualName();
		}
		else {
			return name + "=";
		}
	}

}