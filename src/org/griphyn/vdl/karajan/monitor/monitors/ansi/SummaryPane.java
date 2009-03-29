//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 27, 2009
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import java.io.IOException;
import java.util.Map;
import java.util.TimerTask;

import org.globus.cog.karajan.stack.VariableStack;
import org.griphyn.vdl.karajan.lib.RuntimeStats;
import org.griphyn.vdl.karajan.lib.RuntimeStats.ProgressTicker;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSI;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSIContext;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Container;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Label;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.LevelBars;

public class SummaryPane extends Container {
    private SystemState state;

    private static final String[] STATES = new String[] { "Initializing", "Selecting site",
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

        GlobalTimer.getTimer().schedule(new TimerTask() {
            public void run() {
                update();
            }
        }, 1000, 1000);
    }

    private void update() {
        VariableStack stack = state.getStack();
        if (stack != null) {
            ProgressTicker t = RuntimeStats.getTicker(stack);
            if (t != null) {
                Map summary = t.getSummary();
                for (int i = 0; i < STATES.length; i++) {
                    Object v = summary.get(STATES[i]);
                    if (v != null) {
                        String sv = String.valueOf(v);
                        labels[i].setText(sv);
                        try {
                            bars.setValue(i, Integer.parseInt(sv));
                        }
                        catch (NumberFormatException e) {
                            bars.setValue(i, 0);
                        }
                    }
                    else {
                        labels[i].setText("0");
                        bars.setValue(i, 0);
                    }
                }
            }
        }
        redraw();
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
