//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 6, 2006
 */
package org.globus.cog.karajan.workflow.futures;

import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.arguments.VariableArgumentsListener;

public class ChannelSplitter implements VariableArgumentsListener {
	private final FutureVariableArguments vargs;
	private final FutureVariableArguments[] out;
	
	public ChannelSplitter(FutureVariableArguments vargs, int count) {
		this.vargs = vargs;
		vargs.addListener(this);
		out = new FutureVariableArguments[count];
		for (int i = 0; i < count; i++) {
			out[i] = new FutureVariableArguments();
		}
	}
	
	public FutureVariableArguments[] getOuts() {
		return out;
	}

	public void variableArgumentsChanged(VariableArguments source) {
		FutureVariableArguments in = (FutureVariableArguments) source;
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
