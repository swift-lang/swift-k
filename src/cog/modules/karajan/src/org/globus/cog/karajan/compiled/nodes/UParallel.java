// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.compiled.nodes;

import k.rt.ExecutionException;
import k.rt.KRunnable;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.ThreadSetFixed;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.ContainerScope;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.TrackingScope;
import org.globus.cog.karajan.parser.WrapperNode;

public class UParallel extends CompoundNode {
    private int[] frameSizes;
	
	@Override
    protected Node compileChildren(WrapperNode w, Scope scope) throws CompilationException {
	    frameSizes = new int[w.nodes().size()];
	    // TODO Allocate separate frames for each sub-thread. It's likely that most sub-threads
	    // will finish long before a straggling one. This unnecessarily eats all of the space 
	    // that was allocated on the frame for the finished threads
	    int index = 0;
        for (WrapperNode c : w.nodes()) {
            ContainerScope cs = new ContainerScope(c, scope);
            TrackingScope ts = new TrackingScope(cs);
            ts.setTrackChannels(false);
            ts.setAllowChannelReturns(true);
            ts.setTrackNamed(false);
            ts.setDelayedClosing(true);

            Node n = compileChild(c, ts);
            if (n != null) {
                addChild(n);
                frameSizes[index++] = cs.size();
            }
        }
        if (childCount() == 0) {
            return null;
        }
        else {
            return this;
        }
    }

	
	@Override
	public void run(LWThread thr) throws ExecutionException {
		int state = thr.checkSliceAndPopState();
		Stack stack = thr.getStack();
		ThreadSetFixed ts = (ThreadSetFixed) thr.popState();
		int fc = thr.popIntState();
		try {
			switch (state) {
				case 0:
					fc = stack.frameCount();
					state++;
				case 1:
				    int ec = childCount();
					final ThreadSetFixed tsf = new ThreadSetFixed(ec);
					ts = tsf;
					final int fcf = fc;
					for (int i = 0; i < ec; i++) {
						final int fi = i;
						LWThread ct = thr.fork(i, new KRunnable() {
							@Override
							public void run(LWThread thr) {
								try {
									runChild(fi, thr);
									tsf.threadDone(thr, null);
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
									tsf.threadDone(thr, new ExecutionException(UParallel.this, e));
									tsf.abortAll();
								}
							}
						});
						tsf.add(ct);
						Stack cs = ct.getStack();
                        cs.enter(this, frameSizes[i]);
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
}
