//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 13, 2012
 */
package org.griphyn.vdl.karajan.lib;

import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;

public class CurrentThread extends InternalFunction {
	private ChannelRef<String> cr_vargs;
	
    @Override
    protected Signature getSignature() {
        return new Signature(params(), returns(channel("...", 1)));
    }

    @Override
    public void run(LWThread thr) {
    	cr_vargs.append(thr.getStack(), SwiftFunction.getThreadPrefix(thr));
    }
}
