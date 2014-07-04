//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2013
 */
package org.griphyn.vdl.karajan.monitor.monitors.http;

import java.nio.ByteBuffer;
import java.util.Map;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.items.SummaryItem;

public class SummaryDataBuilder extends StateDataBuilder {
    
    private final SystemState state;
    private int maxCount;
    private JSONEncoder e;

    SummaryDataBuilder(SystemState state) {
        this.state = state;
    }

    @Override
    public ByteBuffer getData(Map<String, String> params) {
        e = new JSONEncoder();
        e.beginMap();
        
        e.writeMapItem("start", state.getStart());
        e.writeMapItem("total", state.getTotal());
        e.writeMapItem("completed", state.getCompleted());
        e.writeMapItem("maxHeap", state.getMaxHeap());
        e.writeMapItem("maxHeapFormatted", state.getMaxHeapFormatted());
        e.writeMapItem("crtHeap", state.getUsedHeap());
        e.writeMapItem("crtHeapFormatted", state.getCurrentHeapFormatted());
        e.writeMapItem("timeLeftFormatted", state.getEstimatedTimeLeftFormatted());
        e.writeMapItem("elapsedTimeFormatetd", state.getElapsedTimeFormatted());
        e.writeMapItem("progressString", state.getGlobalProgressString());
        
        SummaryItem summary = (SummaryItem) state.getItemByID(SummaryItem.ID, StatefulItemClass.WORKFLOW);
        if (summary != null) {
            Map<String, Integer> counts = summary.getCounts(state);
            for (int i = 0; i < SummaryItem.STATES.length; i++) {
                Integer v = counts.get(SummaryItem.STATES[i].getName());
                if (v != null) {
                    if (v > maxCount) {
                        maxCount = v;
                    }
                }
            }
            e.writeMapItem("maxCount", maxCount);
            for (int i = 0; i < SummaryItem.STATES.length; i++) {
                String name = SummaryItem.STATES[i].getName();
                Integer v = counts.get(name);
                if (v != null) {
                    e.writeMapItem(name, v);
                }
                else {
                    e.writeMapItem(name, 0);
                }
            }
        }
        e.endMap();
        return ByteBuffer.wrap(e.toString().getBytes());
    }
}