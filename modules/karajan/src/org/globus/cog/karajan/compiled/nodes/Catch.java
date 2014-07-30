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
 * Created on Nov 8, 2005
 */
package org.globus.cog.karajan.compiled.nodes;

import java.util.LinkedList;

import k.rt.ExecutionException;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.parser.WrapperNode;

public class Catch extends InternalFunction {
	private String name;
	private Node body;
	
	private VarRef<ExecutionException> exception;
	
	@Override
	protected Signature getSignature() {
		return new Signature(params(identifier("name"), block("body")));
	}
	
	
	@Override
	protected void compileBlocks(WrapperNode w, Signature sig, LinkedList<WrapperNode> blocks,
			Scope scope) throws CompilationException {
		Var exv = scope.lookup(Try.EXCEPTION_VAR_NAME);
		exception = scope.getVarRef(exv);
		scope.addAlias(exv, name);
		super.compileBlocks(w, sig, blocks, scope);
	}
	
	@Override
	protected void runBody(LWThread thr) {
		if (body == null) {
			return;
		}
		if (CompilerSettings.PERFORMANCE_COUNTERS) {
			startCount++;
		}
		body.run(thr);
	}	
}
