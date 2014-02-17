//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 6, 2006
 */
package org.globus.cog.karajan.compiled.nodes;

import k.rt.FutureMemoryChannel;
import k.rt.MemoryChannel;
import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.futures.ChannelSplitter;

public class ChannelFork extends InternalFunction {
    private ArgRef<k.rt.Channel<Object>> channel;
    private ArgRef<Number> count;
    
    private ChannelRef<k.rt.Channel<Object>> cr_vargs;
	
	@Override
	protected Signature getSignature() {
		return new Signature(params("channel", "count"), returns(channel("...", DYNAMIC)));
	}



	@Override
	protected void runBody(LWThread thr) {
	    Stack stack = thr.getStack();
	    int count = this.count.getValue(stack).intValue();
		
	    k.rt.Channel<Object> channel = this.channel.getValue(stack);
	    k.rt.Channel<k.rt.Channel<Object>> ret = cr_vargs.get(stack);
		
		if (channel instanceof FutureMemoryChannel) {
			ChannelSplitter<Object> mux = new ChannelSplitter<Object>((FutureMemoryChannel<Object>) channel, count);
			
			for (k.rt.Channel<Object> r : mux.getOuts()) {
			    ret.add(r);
			}
		}
		else {
			for (int i = 0; i < count; i++) {
			    MemoryChannel<Object> r = new MemoryChannel<Object>();
			    
			    r.addAll(channel);
			    
			    ret.add(r);
			}
		}
	}
}
