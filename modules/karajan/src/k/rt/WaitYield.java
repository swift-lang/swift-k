//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 17, 2008
 */
package k.rt;

import java.util.Date;

import k.thr.Yield;

public class WaitYield extends Yield {
    private long delay;

    public WaitYield(int pstate, long delay) {
        super(pstate);
        this.delay = delay;
    }
    
    public WaitYield(int pstate, Date until) {
        super(pstate);
        this.delay = until.getTime() - System.currentTimeMillis();
    }

    public long getDelay() {
        return delay;
    }
}
