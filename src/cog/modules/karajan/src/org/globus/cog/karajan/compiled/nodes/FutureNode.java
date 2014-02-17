// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jun 6, 2003
 *  
 */
package org.globus.cog.karajan.compiled.nodes;

import k.rt.ExecutionException;
import k.rt.FutureObject;
import k.rt.KRunnable;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.futures.FutureEvaluationException;
import org.globus.cog.karajan.parser.WrapperNode;

public class FutureNode extends FramedInternalFunction {
	private ArgRef<Object> value;
	private ChannelRef<Object> cr_vargs;
	
	@Override
	protected Signature getSignature() {
		return new Signature(params("value"), returns(channel("...", 1)));
	}
		
	@Override
	public void runBody(LWThread thr) {
		int ec = childCount();
        int i = thr.checkSliceAndPopState();
        Stack stack = thr.getStack();
        try {
	        switch (i) {
	        	case 0:
	        		initializeArgs(stack);
	        		i++;
	        	default:
			            for (; i <= ec; i++) {
			            	runChild(i - 1, thr);
			            }
		    }
        }
        catch (Yield y) {
            y.getState().push(i);
            throw y;
        }
	}

	@Override
	public void run(LWThread thr) {
		final FutureObject fo = new FutureObject();
		LWThread nt = thr.fork(new KRunnable() {
			@Override
			public void run(LWThread thr) {
				Stack stack = thr.getStack();
				try {
	    			runBody(thr);
	    			fo.setValue(value.getValue(stack));
	    		}
	    		catch (Exception e) {
	    			fo.fail(new FutureEvaluationException(e));
	    		}
			}
		});
		Stack ns = thr.getStack().copy();
		enter(ns);
		cr_vargs.append(ns, fo);
		nt.setStack(ns);
		nt.start();
	}
}