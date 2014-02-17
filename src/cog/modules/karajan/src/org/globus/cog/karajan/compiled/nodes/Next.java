// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 2, 2004
 */
package org.globus.cog.karajan.compiled.nodes;

import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;

public class Next extends InternalFunction {
	private ArgRef<Object> value;
	private ChannelRef<Object> cr_next;
	
	@Override
	protected Signature getSignature() {
		return new Signature(params("value"), returns(channel("next", 1)));
	}
	@Override
	protected void runBody(LWThread thr) {
		Stack stack = thr.getStack();
		cr_next.append(stack, value.getValue(stack));
	}
	
	
}