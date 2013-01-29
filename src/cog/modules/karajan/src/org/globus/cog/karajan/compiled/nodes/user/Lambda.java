//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 30, 2012
 */
package org.globus.cog.karajan.compiled.nodes.user;

import java.util.Iterator;
import java.util.List;

import k.rt.Channel;
import k.rt.Frame;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;


public class Lambda extends UserDefinedFunction {
	private ChannelRef<Object> cr_vargs;
	
	@Override
	public void run(LWThread thr) {
		super.run(thr);
		if (cr_vargs != null) {
			cr_vargs.append(thr.getStack(), this);
		}
	}

	@Override
	protected void _return(Scope scope, Signature sig) {
		Var.Channel ret = scope.parent.lookupChannel("...");
		if (!ret.append(this)) {
			cr_vargs = scope.parent.getChannelRef(ret);
		}
	}
	
	public void runBody(LWThread thr, List<ChannelRef<Object>> referencedChannels,
			Channel<Object> args) {
		int i = thr.checkSliceAndPopState();
		Stack orig = thr.getStack();
		Stack istk = (Stack) thr.popState();
		try {
			switch(i) {
				case 0:
					orig = thr.getStack();
					istk = defStack.copy();
					istk.enter(this, frameSize);
					bindArgs(orig, istk, referencedChannels, args);
					i++;
				case 1:
					thr.setStack(istk);
					if (CompilerSettings.PERFORMANCE_COUNTERS) {
						startCount++;
					}
					body.run(thr);
					thr.setStack(orig);
			}
		}
		catch (RuntimeException e) {
			thr.setStack(orig);
			throw e;
		}
		catch (Yield y) {
			thr.setStack(orig);
			y.getState().push(istk);
			y.getState().push(i);
			throw y;
		}
	}

	private void bindArgs(Stack parent, Stack def, List<ChannelRef<Object>> wrapperChannels,
			Channel<Object> args) {
		if (channelReturnRefs != null) {
			Iterator<ChannelRef<Object>> i1 = wrapperChannels.iterator();
			Iterator<ChannelRef<Object>> i2 = channelReturnRefs.iterator();
			
			while (i1.hasNext()) {
				i2.next().set(def, i1.next().get(parent));
			}
		}
		int index = 0;
		Frame top = def.top();
		for (Object o : args) {
			top.set(index++, o);
		}
	}
}
