// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.compiled.nodes;

import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;

public class Print extends InternalFunction {
	private ArgRef<Boolean> nl;
	private ChannelRef<Object> c_vargs;
	private ChannelRef<Object> cr_stdout;
	
	@Override
	protected Signature getSignature() {
		return new Signature(
				params(optional("nl", Boolean.TRUE), "..."),
				returns(channel("stdout", DYNAMIC))
		);
	}

	public void runBody(LWThread thr) {
		Stack stack = thr.getStack();
		k.rt.Channel<Object> c = c_vargs.get(stack);
		k.rt.Channel<Object> stdout = cr_stdout.get(stack);
		for (Object o : c) {
			stdout.add(o);
		}
		if (nl.getValue(stack)) {
			stdout.add("\n");
		}
	}
}