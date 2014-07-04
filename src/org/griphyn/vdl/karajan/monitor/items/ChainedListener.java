//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 9, 2014
 */
package org.griphyn.vdl.karajan.monitor.items;

public class ChainedListener implements StatefulItem.Listener {
    private StatefulItem.Listener crt, old;
    
    public ChainedListener(StatefulItem.Listener crt, StatefulItem.Listener old) {
        this.crt = crt;
        this.old = old;
    }

    @Override
    public void itemUpdated(StatefulItem item) {
        crt.itemUpdated(item);
        old.itemUpdated(item);
    }
}
