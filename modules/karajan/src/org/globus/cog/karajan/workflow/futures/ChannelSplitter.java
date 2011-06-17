//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 6, 2006
 */
package org.globus.cog.karajan.workflow.futures;

import org.globus.cog.karajan.stack.VariableStack;

/**
 * This class allows splitting of a channel into multiple copies.
 * This allows the split channels to be used independently (including
 * removing objects from the channel).
 * 
 * @author Mihael Hategan
 *
 */
public class ChannelSplitter implements FutureListener {
	private final FutureVariableArguments vargs;
	private final FutureVariableArguments[] out;
	
	public ChannelSplitter(FutureVariableArguments vargs, int count) {
		this.vargs = vargs;
		vargs.addModificationAction(this, null);
		out = new FutureVariableArguments[count];
		for (int i = 0; i < count; i++) {
			out[i] = new FutureVariableArguments();
		}
	}
	
	public FutureVariableArguments[] getOuts() {
		return out;
	}
	
	

	@Override
	public void futureModified(Future f, VariableStack stack) {
		FutureVariableArguments in = (FutureVariableArguments) f;
		while(in.available() > 0) {
			Object o = in.removeFirst();
			for (int i = 0; i < out.length; i++) {
				out[i].append(o);
			}
		}
		if (in.isClosed()) {
			for (int i = 0; i < out.length; i++) {
				out[i].close();
			}
		}
	}
}
