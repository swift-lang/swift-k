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

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.parser.WrapperNode;

public class ChannelTo extends InternalFunction {
	private ChannelRef<Object> dst;
	private ChannelRef<Object> c_vargs;
	private String name;
	private Node body;
	
	@Override
	protected Signature getSignature() {
		return new Signature(params(identifier("name"), block("body")));
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
	public Node compile(WrapperNode w, Scope scope) throws CompilationException {
		Node n = super.compile(w, scope);
		if (c_vargs == null) {
			return null;
		}
		else {
			return n;
		}
	}
	
	@Override
	protected void compileBlocks(WrapperNode w, Signature sig, LinkedList<WrapperNode> blocks,
			Scope scope) throws CompilationException {
		Var.Channel cdst = scope.parent.lookupChannel(name, this);
		Var.Channel csrc = scope.addChannel("...");
		csrc.setValue(cdst.getValue());
		super.compileBlocks(w, sig, blocks, scope);
		if (csrc.isDynamic()) {
			c_vargs = new ChannelRef.Redirect<Object>("...", csrc.getIndex(), scope.parent.getChannelRef(cdst));
		}
	}

	@Override
	protected void initializeArgs(Stack stack) {
		try {
			c_vargs.create(stack);
		}
		catch (RuntimeException e) {
			throw new ExecutionException(this, e);
		}
	}
}