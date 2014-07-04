//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 9, 2014
 */
package org.griphyn.vdl.karajan.monitor.items;

public abstract class AbstractListenableStatefulItem extends AbstractStatefulItem {
    
    private Listener listener;

    public AbstractListenableStatefulItem(String id) {
        super(id);
    }
    
    public void addListener(Listener listener) {
        if (this.listener != null) {
            listener = new ChainedListener(listener, this.listener);
        }
        this.listener = listener;
    }
    
    protected void notifyListener() {
        if (listener != null) {
            listener.itemUpdated(this);
        }
    }
}
