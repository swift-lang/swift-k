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
 * Created on Oct 19, 2004
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;


public class JavaNull extends Node {

	private ChannelRef<Object> cr_vargs;
	
	@Override
	public Node compile(WrapperNode w, Scope scope) throws CompilationException {
		Node fn = super.compile(w, scope);
		Var.Channel vargs = scope.lookupChannel("...");
		if (vargs.append(null)) {
			return null;
		}
		else {
			cr_vargs = scope.getChannelRef(vargs);
			return fn;
		}
	}

	@Override
	public void run(LWThread thr) {
		cr_vargs.append(thr.getStack(), null);
	}
}
