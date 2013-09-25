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
import java.util.TimerTask;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.items.SummaryItem;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSI;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSIContext;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Container;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Dialog;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Label;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.LevelBars;

public class SummaryPane extends Container {
    private SystemState state;

    public static final String[] STATES = new String[] { "Initializing", "Selecting site",
            "Stage in", "Submitting", "Submitted", "Active", "Stage out",
            "Failed", "Replicating", "Finished successfully" };
    
    private Label[] labels;
    private LevelBars bars;

    public SummaryPane(SystemState state) {
        this.state = state;
        bars = new LevelBars(STATES.length);
        bars.setLocation(34, 2);
        add(bars);
        labels = new Label[STATES.length];
        for (int i = 0; i < STATES.length; i++) {
            addLabel(STATES[i] + ": ", 2, 2 + i, 24);
            labels[i] = addLabel("0", 25, 2 + i, 8);
        }

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
                Map<String, Integer> counts = summary.getCounts(state);
                for (int i = 0; i < STATES.length; i++) {
                    Integer v = counts.get(STATES[i]);
                    if (v != null) {
                        labels[i].setText(v.toString());
                        bars.setValue(i, v);
                    }
                    else {
                        labels[i].setText("0");
                        bars.setValue(i, 0);
                    }
                }
            }
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
        bars.setSize(width - 35, STATES.length);
        super.validate();
    }    
}
