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
 * Created on Sep 29, 2004
 */
package org.globus.cog.karajan.compiled.nodes;

import java.util.List;

import k.rt.ExecutionException;
import k.rt.KRunnable;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.ThreadSet;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.TrackingScope;
import org.globus.cog.karajan.analyzer.Var;

public class Race extends OrderedChannelsNode {

	@Override
	public void run(LWThread thr) {
		int state = thr.checkSliceAndPopState(2);
		Stack stack = thr.getStack();
		ThreadSet ts = (ThreadSet) thr.popState();
		int fc = thr.popIntState();
		try {
			switch (state) {
				case 0:
					fc = stack.frameCount();
					state++;
				case 1:
					final ThreadSet tsf = new ThreadSet();
					ts = tsf;
					final int fcf = fc;
					int ec = childCount();
					for (int i = 0; i < ec; i++) {
						final int fi = i;
						LWThread ct = thr.fork(new KRunnable() {
							@Override
							public void run(LWThread thr) {
								try {
									runChild(fi, thr);
									synchronized(tsf) {
										if (!tsf.anyDone()) {
											Race.this.getOCW(fi).closeArgs(thr.getStack());
											tsf.abortAll();
										}
									}
								}
								catch (ExecutionException e) {
									Stack stack = thr.getStack();
									stack.dropToFrame(fcf);
									tsf.threadDone(thr, e);
									tsf.abortAll();
								}
								catch (RuntimeException e) {
									Stack stack = thr.getStack();
									stack.dropToFrame(fcf);
									tsf.threadDone(thr, new ExecutionException(Race.this, e));
									tsf.abortAll();
								}
							}
						});
						tsf.add(ct);
						initializeBuffers(i, stack);
					}
					ts.startAll();
					state++;
				default:
					ts.waitFor();
			}
		}
		catch (Yield y) {
			y.getState().push(fc);
			y.getState().push(ts);
			y.getState().push(state, 2);
			throw y;
		}
	}
	
	private void processChannels(List<TrackingScope> lts, Scope scope) {
		int childIndex = 0;
		for (TrackingScope ts : lts) {
			if (ts.getReferencedChannels() != null) {
				for (String cn : ts.getReferencedChannels().keySet()) {
					Var.Channel src = ts.getChannel(cn);
					Var.Channel dst = scope.lookupChannel(cn);
					ChannelRef.Buffer<Object> buf = new ChannelRef.Buffer<Object>(cn, src.getIndex(), dst.getIndex());
					getOCW(childIndex).addChannel(buf);
				}
			}
			
			childIndex++;
			ts.close();
		}
	}
	
	protected OrderedChannelsWrapper newOrderedChannelsWrapper(Node n) {
		return new OrderedChannelsWrapper(n, false);
	}
}