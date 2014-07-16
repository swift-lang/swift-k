//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 17, 2008
 */
package k.rt;

import k.thr.Yield;

public class ConditionalYield extends Yield {
    private Future f;
    private final int sequence;

    public ConditionalYield(Future f) {
        this.f = f;
        this.sequence = -1;
    }
    
    public ConditionalYield(Future f, int sequence) {
        this.f = f;
        this.sequence = sequence;
    }
    
    public ConditionalYield(int pstate, Future f) {
    	super(pstate);
        this.f = f;
        this.sequence = -1;
    }

    public Future getFuture() {
        return f;
    }

	public int getSequence() {
		return sequence;
	}
}
