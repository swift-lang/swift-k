//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 22, 2004
 */
package org.globus.cog.karajan.compiled.nodes;

import k.rt.ExecutionException;
import k.rt.KRunnable;
import k.thr.LWThread;


public class UnsynchronizedNode extends Sequential {
		
	@Override
	public void run(LWThread thr) {
		LWThread nt = thr.fork(new KRunnable() {
			@Override
			public void run(LWThread thr) {
				try {
	    			UnsynchronizedNode.super.run(thr);
	    		}
	    		catch (ExecutionException e) {
	    		}
			}
		});
		nt.start();
	}
}
