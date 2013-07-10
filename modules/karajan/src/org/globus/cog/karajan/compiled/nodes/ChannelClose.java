//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 6, 2006
 */
package org.globus.cog.karajan.compiled.nodes;

import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;

public class ChannelClose extends InternalFunction {
	private ArgRef<k.rt.Channel<?>> channel;
	
	
    @Override
	protected Signature getSignature() {
		return new Signature(params("channel"));
	}

	@Override
	protected void runBody(LWThread thr) {
		channel.getValue(thr.getStack()).close();
	}
}
