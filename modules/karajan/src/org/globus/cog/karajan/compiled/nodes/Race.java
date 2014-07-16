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
		int state = thr.checkSliceAndPopState();
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
				case 2:
					ts.waitFor();
			}
		}
		catch (Yield y) {
			y.getState().push(fc);
			y.getState().push(ts);
			y.getState().push(state);
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