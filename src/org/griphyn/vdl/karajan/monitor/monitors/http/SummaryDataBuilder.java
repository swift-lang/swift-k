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

    SummaryDataBuilder(SystemState state) {
        this.state = state;
    }

    @Override
    public ByteBuffer getData(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        add(sb, "start", state.getStart());
        add(sb, "total", state.getTotal());
        add(sb, "completed", state.getCompleted());
        add(sb, "maxHeap", state.getMaxHeap());
        add(sb, "maxHeapFormatted", state.getMaxHeapFormatted());
        add(sb, "crtHeap", state.getCurrentHeap());
        add(sb, "crtHeapFormatted", state.getCurrentHeapFormatted());
        add(sb, "timeLeftFormatted", state.getEstimatedTimeLeftFormatted());
        add(sb, "elapsedTimeFormatetd", state.getElapsedTimeFormatted());
        add(sb, "progressString", state.getGlobalProgressString());
        
        SummaryItem summary = (SummaryItem) state.getItemByID(SummaryItem.ID, StatefulItemClass.WORKFLOW);
        if (summary != null) {
            Map<String, Integer> counts = summary.getCounts(state);
            for (int i = 0; i < SummaryItem.STATES.length; i++) {
                Integer v = counts.get(SummaryItem.STATES[i]);
                if (v != null) {
                    if (v > maxCount) {
                        maxCount = v;
                    }
                }
            }
            add(sb, "maxCount", maxCount);
            for (int i = 0; i < SummaryItem.STATES.length; i++) {
                Integer v = counts.get(SummaryItem.STATES[i]);
                if (v != null) {
                    add(sb, SummaryItem.STATES[i].getName(), v);
                }
                else {
                    add(sb, SummaryItem.STATES[i].getName(), 0);
                }
            }
        }
        return ByteBuffer.wrap(sb.toString().getBytes());
    }
}