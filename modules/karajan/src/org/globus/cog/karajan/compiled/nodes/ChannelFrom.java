// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 8, 2005
 */
package org.globus.cog.karajan.compiled.nodes;

import java.util.LinkedList;

import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.parser.WrapperNode;

public class ChannelFrom extends InternalFunction {
	private String name;
	private Node body;
	private ChannelRef<Object> cr_vargs;
	private ChannelRef<Object> channel;

	
	@Override
	protected Signature getSignature() {
		return new Signature(params(identifier("name"), block("body")), returns(channel("...", DYNAMIC)));
	}
	
	@Override
	protected void runBody(LWThread thr) {
		if (body != null) {
			if (CompilerSettings.PERFORMANCE_COUNTERS) {
				startCount++;
			}
			body.run(thr);
		}
	}

	@Override
	protected void initializeArgs(Stack stack) {
		channel.set(stack, cr_vargs.get(stack));
	}

	@Override
	protected void compileBlocks(WrapperNode w, Signature sig, LinkedList<WrapperNode> blocks,
			Scope scope) throws CompilationException {
		Var.Channel csrc = scope.addChannel(name);
		Var.Channel cdst = scope.parent.lookupChannel("...");
		csrc.setValue(cdst.getValue());
		channel = new ChannelRef.Dynamic<Object>(name, csrc.getIndex());
		super.compileBlocks(w, sig, blocks, scope);
	}
}