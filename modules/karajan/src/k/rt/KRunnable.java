//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 14, 2008
 */
package k.rt;

import k.thr.LWThread;


public interface KRunnable {
    public void run(LWThread thr);
}
