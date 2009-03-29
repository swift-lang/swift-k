/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.items;


public class TraceItem extends AbstractStatefulItem {
    private int started, ended;
	
	public TraceItem(String id) {
		super(id);
	}

	public StatefulItemClass getItemClass() {
		return StatefulItemClass.TRACE;
	}

    public synchronized void incStarted() {
        started++;
    }

    public synchronized void incEnded() {
        ended++;
    }

    public int getStarted() {
        return started;
    }

    public void setStarted(int started) {
        this.started = started;
    }

    public int getEnded() {
        return ended;
    }

    public void setEnded(int ended) {
        this.ended = ended;
    }
}
