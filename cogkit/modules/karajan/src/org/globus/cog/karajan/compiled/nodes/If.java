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
 * 
 * Created on Mar 27, 2004
 *  
 */
package org.globus.cog.karajan.compiled.nodes;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.DynamicScope;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.SingleValuedChannel;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.parser.WrapperNode;

public class If extends CompoundNode {
	private ChannelRef.SingleValued<Boolean> c_vargs;
	private Node bthen;
	private Node belse;
	
	@Override
	protected Node compileChildren(WrapperNode w, Scope scope) throws CompilationException {
		int nc = w.nodeCount();
		
		WrapperNode wthen = null;
		WrapperNode welse = null;
		
		if (nc > 3) {
			throw new CompilationException(w, "Invalid number of arguments");
		}
		else if (nc == 3) {
			wthen = w.getNode(nc - 2);
			welse = w.getNode(nc - 1);
			nc = 1;
		}
		else if (nc == 2) {
			wthen = w.getNode(1);
			nc = 1;
		}
		else {
			throw new CompilationException(w, "Missing 'then' block");
		}
		
		Object condition = compileCondition(w, nc, scope);
		
		
		if (condition != null) {
			return compileStatic(condition, w, wthen, welse, scope);
		}
		
		bthen = compileBranch(wthen, scope, false);
		if (welse != null) {
			belse = compileBranch(welse, scope, false);
		}
		if (bthen == null && belse == null) {
			// TODO side-effects in condition
			return null;
		}
		else {
			return this;
		}
	}

	private Node compileBranch(WrapperNode branch, Scope scope, boolean staticScope) throws CompilationException {
		Scope cs;
		if (staticScope) {
			cs = new Scope(branch, scope);
		}
		else {
			cs = new DynamicScope(branch, scope);
		}
		Node fn = compileChild(branch, cs);
		if (fn != null) {
			fn.setParent(this);
		}
		return fn;
	}

	private Node compileStatic(Object condition, WrapperNode w, WrapperNode wthen, WrapperNode welse, Scope scope) 
			throws CompilationException {
		if (condition instanceof Boolean) {
			if ((Boolean) condition) {
				return compileBranch(wthen, scope, true);
			}
			else {
				if (welse != null) {
					return compileBranch(welse, scope, true);
				}
				else {
					return null;
				}
			}
		}
		else {
			throw new CompilationException(w, "Condition must be a boolean value");
		}
	}
	
	private static final List<Param> EP = Collections.emptyList();

	private Object compileCondition(WrapperNode w, int nc, Scope scope) throws CompilationException {
		Scope ns = new Scope(w, scope);
		SingleValuedChannel svc = new SingleValuedChannel(w);
		Var.Channel vargs = ns.addChannel("...", svc);
		c_vargs = new ChannelRef.SingleValued<Boolean>("condition", vargs.getIndex());
		boolean pure = true;
		for (int i = 0; i < nc; i++) {
			Node n = compileChild(w.getNode(i), ns);
			if (n != null) {
				pure = false;
				addChild(n);
			}
		}
		ns.close();
		vargs.disable();
		if (!vargs.isDynamic() && svc.getValue() != null && pure) {
			return svc.getValue();
		}
		else {
			return null;
		}
	}

	@Override
	public void run(LWThread thr) {
	    int nc = childCount();
		int i = thr.checkSliceAndPopState(nc + 2);
		boolean condition = thr.popBoolean();
		
		Stack stack = thr.getStack();
		try {
			switch (i) {
				case 0:
					initializeArgs(stack);
					i++;
				default:
					for (; i <= nc; i++) {
						runChild(i - 1, thr);
					}
					condition = c_vargs.getValue(stack);
					i = Integer.MAX_VALUE;
				case Integer.MAX_VALUE:
					if (condition) {
						if (CompilerSettings.PERFORMANCE_COUNTERS) {
							startCount++;
						}
						if (bthen != null) {
							bthen.run(thr);
						}
					}
					else if (belse != null) {
						if (CompilerSettings.PERFORMANCE_COUNTERS) {
							startCount++;
						}
						belse.run(thr);
					}
			}
		}
		catch (Yield y) {
			y.getState().push(condition);
			y.getState().push(i, nc + 2);
			throw y;
		}
	}

	private void initializeArgs(Stack stack) {
		c_vargs.create(stack);
	}

	@Override
	public void dump(PrintStream ps, int level) throws IOException {
		super.dump(ps, level);
		if (bthen != null) {
			bthen.dump(ps, level + 1);
		}
		else {
			for (int i = 0; i < level; i++) {
				ps.print("\t");
			}
			ps.println("-");
		}
		if (belse != null) {
			belse.dump(ps, level + 1);
		}
	}
}
