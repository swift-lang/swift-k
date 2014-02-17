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
import k.thr.ThreadSet;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.TrackingScope;
import org.globus.cog.karajan.parser.WrapperNode;

public class UParallel extends CompoundNode {
	
	@Override
    protected Node compileChildren(WrapperNode w, Scope scope) throws CompilationException {
        for (WrapperNode c : w.nodes()) {
            TrackingScope ts = new TrackingScope(scope);
            ts.setTrackChannels(false);
            ts.setAllowChannelReturns(true);
            ts.setTrackNamed(false);
            ts.setDelayedClosing(true);

            Node n = compileChild(c, ts);
            if (n != null) {
                addChild(n);
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
