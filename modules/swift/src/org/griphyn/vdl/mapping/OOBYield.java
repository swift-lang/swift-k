//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 28, 2013
 */
package org.griphyn.vdl.mapping;

import k.thr.Yield;

public class OOBYield extends HandleOpenException {
    private final Yield y;
    
    public OOBYield(Yield y, DSHandle h) {
        super(h);
        this.y = y;
    }
    
    public Yield wrapped() {
        return y;
    }
    
    public Yield wrapped(Object traceElement) {
        y.getState().addTraceElement(traceElement);
        return y;
    }
}
