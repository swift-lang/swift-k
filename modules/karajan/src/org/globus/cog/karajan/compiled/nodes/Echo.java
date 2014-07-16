// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.compiled.nodes;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.parser.WrapperNode;
import org.globus.cog.karajan.util.TypeUtil;

public class Echo extends InternalFunction {
	
	private ArgRef<Boolean> nl;
	private ChannelRef<Object> c_vargs;
	
	@Override
	protected Signature getSignature() {
		return new Signature(
				params(optional("nl", Boolean.TRUE), "...")
		);
	}

	public void runBody(LWThread thr) {
		Stack stack = thr.getStack();
		k.rt.Channel<Object> c = c_vargs.get(stack);
		for (Object o : c) {
			waitFor(o);
		}
		for (Object o : c) {
			System.out.print(TypeUtil.toString(o));
		}
		if (nl.getValue(stack)) {
			System.out.println();
		}
	}	
}