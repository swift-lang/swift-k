/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import java.io.IOException;
import java.util.Map;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.common.GlobalTimer;
import org.griphyn.vdl.karajan.monitor.items.ApplicationState;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.items.SummaryItem;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSI;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSIContext;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Container;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Dialog;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Label;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.LevelBar;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.LevelBars;

public class SummaryPane extends Container {
    private SystemState state;
    
    private LevelBars bars;
    private LevelBar memory;
    private Label memlabel;

    public SummaryPane(SystemState state) {
        this.state = state;
        bars = new LevelBars(SummaryItem.STATES.length);
        bars.setLocation(26, 2);
        add(bars);
        for (int i = 0; i < SummaryItem.STATES.length; i++) {
            addLabel(SummaryItem.STATES[i] + ": ", 2, 2 + i, 24);
        }
        
        memlabel = addLabel("Heap: ", 2, 4 + SummaryItem.STATES.length, 24);
        memory = new LevelBar();
        memory.setLocation(26, 4 + SummaryItem.STATES.length);
        add(memory);

        GlobalTimer.getTimer().schedule(new SafeTimerTask(getScreen()) {
            public void runTask() {
                update();
            }
        }, 1000, 1000);
    }

    private void update() {
        try {
            SummaryItem summary = (SummaryItem) state.getItemByID(SummaryItem.ID, StatefulItemClass.WORKFLOW);
            if (summary != null) {
                Map<ApplicationState, Integer> counts = summary.getCounts(state);
                bars.setRange(state.getTotal());
                for (int i = 0; i < SummaryItem.STATES.length; i++) {
                    if (SummaryItem.STATES[i].isEnabled()) {
                        Integer v = counts.get(SummaryItem.STATES[i]);
                        int iv = 0, prev = 0;
                        if (v != null) {
                            iv = v.intValue();
                        }
                        
                        if (SummaryItem.STATES[i] == ApplicationState.FINISHED_SUCCESSFULLY) {
                            v = counts.get(ApplicationState.FINISHED_IN_PREVIOUS_RUN);
                            if (v != null) {
                                prev = v.intValue();
                            }
                        }
                        
                        if (prev == 0) {
                            bars.setValue(i, iv);
                            bars.setText(i, String.valueOf(iv));
                        }
                        else {
                            bars.setValue(i, iv + prev);
                            bars.setOtherValue(i, prev);
                            bars.setText(i, String.valueOf(iv + prev) + " (" + String.valueOf(prev) + ")");
                        }
                    }
                }
            }
            // mem
            Runtime r = Runtime.getRuntime();
            long heapMax = state.getMaxHeap();
            long heapCrt = state.getUsedHeap();
            double fraction = (double) heapCrt / heapMax;
            memory.setValue((float) fraction);
            memory.setText(state.getCurrentHeapFormatted() + " / " + state.getMaxHeapFormatted());
            redraw();
        }
        catch (Exception e) {
            Dialog.displaySimpleDialog(getScreen(), "Error", e.toString(), new String[] {"Close"});
        }
    }
    
    private Label addLabel(String text, int x, int y, int w) {
        Label l = new Label(text);
        l.setLocation(x, y);
        l.setSize(w, 1);
        l.setJustification(Label.RIGHT);
        l.setBgColor(ANSI.CYAN);
        l.setFgColor(ANSI.BLACK);
        add(l);
        return l;
    }

    protected void draw(ANSIContext context) throws IOException {
        context.filledRect(sx, sy, width, height);
    }

    protected void validate() {
        bars.setSize(width - 27, SummaryItem.STATES.length);
        memory.setSize(width - 27, 1);
        super.validate();
    }    
}
