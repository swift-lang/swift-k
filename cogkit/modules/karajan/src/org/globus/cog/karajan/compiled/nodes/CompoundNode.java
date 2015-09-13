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
 * Created on Dec 16, 2012
 */
package org.globus.cog.karajan.compiled.nodes;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import k.rt.ExecutionException;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.parser.WrapperNode;

public class CompoundNode extends Node {	
	private List<Node> children;
	
	public CompoundNode() {
		children = Collections.emptyList();
	}
	
	public Node compile(WrapperNode w, Scope scope) throws CompilationException {
		Scope cs = newScope(w, scope);
		Integer l = (Integer) WrapperNode.getTreeProperty(WrapperNode.LINE, w);
		if (l != null) {
			setLine(l);
		}
		setType(w.getNodeType());
		Node fn = compileChildren(w, cs);
		cs.close();
		return fn;
	}
	
	protected Scope newScope(WrapperNode w, Scope scope) {
		return new Scope(w, scope);
	}

	protected Node compileChildren(WrapperNode w, Scope scope) throws CompilationException {
		for (WrapperNode c : w.nodes()) {
			Scope.Checkpoint sc = scope.checkpoint();
			Node n = compileChild(c, scope);
			if (n != null) {
				addChild(n);
			}
			else {
				scope.restore(sc);
			}
		}
		if (childCount() == 0) {
			return null;
		}
		else {
			return this;
		}
	}

	protected Node compileChild(WrapperNode c, Scope scope) throws CompilationException {
		return c.compile(this, scope);
	}
	
	public List<Node> children() {
		return children;
	}

	public synchronized void addChild(Node child) {
		if (children.isEmpty()) {
			children = new ArrayList<Node>();
		}
		children.add(child);
	}

	public Node getChild(int index) {
		return children.get(index);
	}
	
	protected void runChild(int index, LWThread thr) {
		if (CompilerSettings.PERFORMANCE_COUNTERS) {
			startCount++;
		}
	    Node fn = children.get(index);
	    try {
	    	fn.run(thr);
	    }
	    catch (ExecutionException e) {
	        throw e;
	    }
	    catch (RuntimeException e) {
	        throw new ExecutionException(fn, e);
	    }
	}

	public int childCount() {
		return children.size();
	}

	@Override
	public void dump(PrintStream ps, int level) throws IOException {
		super.dump(ps, level);
		for (Node n : children()) {
			n.dump(ps, level + 1);
		}
	}
}
