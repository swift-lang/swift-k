// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.compiled.nodes;

import k.thr.LWThread;
import k.thr.Yield;

import org.apache.log4j.Logger;

public class Sequential extends CompoundNode {
	private static final Logger logger = Logger.getLogger(Sequential.class);
	
	public void run(LWThread thr) {
	    int ec = childCount();
	    int i = thr.checkSliceAndPopState();
	    try {
    	    for (; i < ec; i++) {
    	    	runChild(i, thr);
    	    }
	    }
	    catch (Yield y) {
	    	y.getState().push(i);
	    	throw y;
	    }
	}
}
