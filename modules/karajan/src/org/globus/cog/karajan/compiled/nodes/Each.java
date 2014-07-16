//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 27, 2006
 */
package org.globus.cog.karajan.compiled.nodes;

import java.util.Iterator;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;

public class Each extends InternalFunction {
	private ArgRef<Iterable<Object>> items;
	private ChannelRef<Object> cr_vargs;
	
	@Override
	protected Signature getSignature() {
		return new Signature(params("items"), returns(channel("...", DYNAMIC)));
	}

	@Override
	protected void runBody(LWThread thr) {
		Stack stack = thr.getStack();
		Iterable<Object> items = this.items.getValue(stack);
		Iterator<Object> i = items.iterator();
		while (i.hasNext()) {
			cr_vargs.append(stack, i.next());
		}
	}

}
