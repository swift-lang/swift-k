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
import java.util.List;

import k.rt.ExecutionException;
import k.rt.KRunnable;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.ThreadSet;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.ContainerScope;
import org.globus.cog.karajan.analyzer.DynamicScope;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.TrackingScope;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.parser.WrapperNode;


public class ParallelFor extends InternalFunction {

	protected String name;
	protected ArgRef<Iterable<Object>> in;
	protected Node body;
	
	protected VarRef<Object> var;
	
	protected List<ChannelRef.OrderedFramed<Object>> channels;
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
		TrackingScope ts = new TrackingScope(cs);
		ts.setTrackChannels(true);
		ts.setTrackNamed(false);
		ts.setFixedAllocation(true);
		
		super.compileBlocks(w, sig, blocks, ts);
		
		if (ts.getReferencedChannels() != null) {		

			for (String name : ts.getReferencedChannels().keySet()) {
				Var.Channel dst = scope.lookupChannel(name);
				if (dst.isCommutative()) {
					continue;
				}
				dst.appendDynamic();
				
				ChannelRef<Object> dstref = cs.getChannelRef(dst);
				Var.Channel src = ts.getChannel(name);
				
				if (channels == null) {
					channels = new LinkedList<ChannelRef.OrderedFramed<Object>>();
				}
				channels.add(new ChannelRef.OrderedFramed<Object>(name, src.getIndex(), dstref));
			}
		}
		
		ts.close();
		ds.close();
		frameSize = cs.size();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void runBody(final LWThread thr) {		
		int i = thr.checkSliceAndPopState(2);
		Iterator<Object> it = (Iterator<Object>) thr.popState();
		ThreadSet ts = (ThreadSet) thr.popState();
		int fc = thr.popIntState();
		Stack prev = (Stack) thr.popState();
		Stack stack = thr.getStack();
		try {
			switch(i) {
				case 0:
					it = in.getValue(stack).iterator();
					ts = new ThreadSet();
					ts.lock();
					fc = stack.frameCount() + 1;
					i++;
				case 1:
					final ThreadSet tsf = ts;
					
					ts.checkFailed();
					final int fcf = fc;
					while (it.hasNext()) {
						LWThread ct = thr.fork(new KRunnable() {
							@Override
							public void run(LWThread thr2) {
								try {
									if (CompilerSettings.PERFORMANCE_COUNTERS) {
										startCount++;
									}
									body.run(thr2);
									closeBuffers(thr2.getStack());
									tsf.threadDone(thr2, null);
								}
								catch (Exception e) {
									thr2.getStack().dropToFrame(fcf);
									closeBuffers(thr2.getStack());
									tsf.threadDone(thr2, new ExecutionException(ParallelFor.this, e));
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
						addChannelBuffers(cs, prev);
						var.setValue(cs, it.next());
						prev = cs;
						ct.start();
					}
					ts.unlock();
					i++;
				default:
					ts.waitFor();
			}
		}
		catch (Yield y) {
			y.getState().push(prev);
			y.getState().push(fc);
			y.getState().push(ts);
			y.getState().push(it);
			y.getState().push(i, 2);
			throw y;
		}
	}

	protected void closeBuffers(Stack stack) {
		if (channels != null) {
			for (ChannelRef.OrderedFramed<Object> c : channels) {
				c.close(stack);
			}
		}
	}

	protected void addChannelBuffers(Stack stack, Stack prev) {
		if (channels != null) {
			for (ChannelRef.OrderedFramed<Object> c : channels) {
				c.create(stack, prev);
			}
		}
	}
}
