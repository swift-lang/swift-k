/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.griphyn.vdl.karajan.lib;

import java.lang.reflect.Array;

import k.rt.Stack;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.compiled.nodes.functions.GetAndDeleteVariables;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;

public class WaitFieldValue extends Node {
	final static Logger logger = Logger.getLogger(GetAndDeleteVariables.class);
	
	private VarRef<AbstractDataNode>[] refs;

	@Override
	public void run(LWThread thr) {
		Stack stack = thr.getStack();
				
		for (VarRef<AbstractDataNode> ref : refs) {
		    AbstractDataNode node = ref.getValue(stack);
		    node.waitForAll(this);
		}		
	}

	@SuppressWarnings("unchecked")
    @Override
	public Node compile(WrapperNode wn, Scope scope) throws CompilationException {
		super.compile(wn, scope);

		refs = (VarRef<AbstractDataNode>[]) Array.newInstance(VarRef.class, wn.nodeCount());
		for (int i = 0; i < wn.nodeCount(); i++) {
			String name = wn.getNode(i).getText();
			refs[i] = scope.getVarRef(name);
		}
		return this;
	}

}
