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
 * Created on Jul 7, 2003
 */
package org.globus.cog.karajan.compiled.nodes;

import java.util.Iterator;
import java.util.LinkedList;

import k.rt.ExecutionException;
import k.rt.KRunnable;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.ThreadSet;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.ContainerScope;
import org.globus.cog.karajan.analyzer.DynamicScope;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.parser.WrapperNode;


public class UParallelFor extends InternalFunction {
	protected String name;
	protected ArgRef<Iterable<Object>> in;
	protected Node body;
	
	protected VarRef<Object> var;
	
	protected int frameSize;
	
	@Override
	protected Signature getSignature() {
		return new Signature(params(identifier("name"), "in", block("body")));
	}
	
	@Override
	protected void compileBlocks(WrapperNode w, Signature sig, LinkedList<WrapperNode> blocks,
			Scope scope) throws CompilationException {
		DynamicScope ds = new DynamicScope(w, scope);
		ContainerScope cs = new ContainerScope(w, ds);
		Var v = cs.addVar(name);
		var = cs.getVarRef(v);
		
		super.compileBlocks(w, sig, blocks, cs);
		
		frameSize = cs.size();
		
		ds.close();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void runBody(final LWThread thr) {		
		int i = thr.checkSliceAndPopState(2);
		Iterator<Object> it = (Iterator<Object>) thr.popState();
		ThreadSet ts = (ThreadSet) thr.popState();
		Stack stack = thr.getStack();
		try {
			switch(i) {
				case 0:
					it = in.getValue(stack).iterator();
					ts = new ThreadSet();
					ts.lock();
					i++;
				case 1:
					final ThreadSet tsf = ts;
					ts.checkFailed();
					
					while (it.hasNext()) {
						LWThread ct = thr.fork(new KRunnable() {
							@Override
							public void run(LWThread thr2) {
								try {
									if (CompilerSettings.PERFORMANCE_COUNTERS) {
										startCount++;
									}
									body.run(thr2);
									tsf.threadDone(thr2, null);
								}
								catch (Exception e) {
									tsf.threadDone(thr2, new ExecutionException(UParallelFor.this, e));
									tsf.abortAll();
									thr.awake();
								}
							}
						});
						if(ts.add(ct)) {
							break;
						}
						Stack cs = ct.getStack();
						cs.enter(this, frameSize);
						var.setValue(cs, it.next());
						ct.start();
					}
					ts.unlock();
					i++;
				default:
					ts.waitFor();
			}
		}
		catch (Yield y) {
			y.getState().push(ts);
			y.getState().push(it);
			y.getState().push(i, 2);
			throw y;
		}
	}	
}
