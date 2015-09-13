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
 * Created on Apr 22, 2005
 */
package org.globus.cog.karajan.compiled.nodes;

import k.rt.FutureMemoryChannel;
import k.rt.KRunnable;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.futures.FutureEvaluationException;

public class FutureChannelNode extends FramedInternalFunction {
	private ChannelRef<Object> c_vargs;
	private ChannelRef<Object> cr_vargs;
	
	@Override
	protected Signature getSignature() {
		return new Signature(params("..."), returns(channel("...", 1)));
	}
	
	@Override
	public void runBody(LWThread thr) {
		int ec = childCount();
        int i = thr.checkSliceAndPopState(ec);
        Stack stack = thr.getStack();
        try {
	        for (; i < ec; i++) {
	        	runChild(i, thr);
	        }
        }
        catch (Yield y) {
            y.getState().push(i, ec);
            throw y;
        }
	}

	@Override
	public void run(LWThread thr) {
		LWThread nt = thr.fork(new KRunnable() {
			@Override
			public void run(LWThread thr2) {
				Stack stack = thr2.getStack();
				try {
	    			runBody(thr2);
	    			c_vargs.get(stack).close();
	    		}
	    		catch (Exception e) {
	    			((FutureMemoryChannel<Object>) c_vargs.get(stack)).fail(new FutureEvaluationException(e));
	    		}
			}
		});
		Stack ns = thr.getStack().copy();
		enter(ns);
		FutureMemoryChannel<Object> c = new FutureMemoryChannel<Object>();
		c_vargs.set(ns, c);
		cr_vargs.append(ns, c);
		nt.setStack(ns);
		nt.start();
	}
}
