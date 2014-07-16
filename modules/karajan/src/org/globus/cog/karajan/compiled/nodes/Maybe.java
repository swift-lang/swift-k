//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 26, 2005
 */
package org.globus.cog.karajan.compiled.nodes;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;


public class Maybe extends Try {
	
	public void run(LWThread thr) {
        int i = thr.checkSliceAndPopState();
        int fc = thr.popIntState();
        Stack stack = thr.getStack();
        int ec = childCount();
        try {
        	switch (i) {
        		case 0:
        			fc = stack.frameCount();
        			i++;
        		default:
        			addBuffers(stack);
        			try {
        				for (; i <= ec; i++) {
        					runChild(i - 1, thr);
        				}
        				commitBuffers(stack);
        			}
        			catch (ExecutionException e) {
        				stack.dropToFrame(fc);
        			}
        	}
        }
        catch (Yield y) {
            y.getState().push(fc);
            y.getState().push(i);
            throw y;
        }
    }
}
