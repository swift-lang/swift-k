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

import org.griphyn.vdl.karajan.monitor.Stats;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.common.GlobalTimer;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSI;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSIContext;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Container;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Graph;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Label;

public class TaskStatsPane extends Container {
    private SystemState state;
    private Label[] hl, vl;
    private Label[] totals, currents, periods;
    private Graph[] cg, pg;

    public TaskStatsPane(SystemState state) {
        this.state = state;
        int vsz = height/3;
        int hsz = width/6;
        hl = new Label[] {
                addLabel("Apps ", 11, 1),
                addLabel("Tasks ", 11 + hsz, 1),
                addLabel("Jobs ", 11 + hsz*2, 1),
                addLabel("Transfers ", 11 + hsz*3, 1),
                addLabel("File Ops ", 11 + hsz*4, 1)
        };
        addLabel("Total", 1, 3);
        vl = new Label[] {
                addLabel("Current", 1, 5),
                addLabel("Rate/s", 1, 5 + vsz)
        };
        totals = new Label[5];
        currents = new Label[5];
        periods = new Label[5];
        cg = new Graph[5];
        pg = new Graph[5];
        for (int i = 0; i < 5; i++) {
            totals[i] = addLabel("-", 11 + i * hsz, 3);
            currents[i] = addLabel("-", 11 + i * hsz, 5);
            periods[i] = addLabel("-", 11 + i * hsz, 5 + vsz);
            cg[i] = addGraph(12 + i * hsz, 6, hsz, vsz);
            pg[i] = addGraph(12 + i * hsz, 6 + vsz, hsz, vsz);
        }
        GlobalTimer.getTimer().schedule(new SafeTimerTask(getScreen()) {
            public void runTask() {
                update(); 
            }
        }, 1000, 1000);
    }
    
    protected void validate() {
        super.validate();
        int vsz = (height - 10) / 2;
        int hsz = (width - 12) / 5;
        for (int i = 0; i < hl.length; i++) {
            hl[i].setLocation(11 + hsz * i, 1);
        }
        for (int i = 0; i < vl.length; i++) {
            vl[i].setLocation(1, 5 + i * vsz);
        }
        for (int i = 0; i < periods.length; i++) {
            totals[i].setLocation(11 + hsz * i, 3);
            currents[i].setLocation(11 + hsz * i, 5);
            periods[i].setLocation(11 + hsz * i, 5 + vsz);
            cg[i].setLocation(11 + hsz * i, 6);
            pg[i].setLocation(11 + hsz * i, 6 + vsz);
            cg[i].setSize(hsz - 1, vsz - 1);
            pg[i].setSize(hsz - 1, vsz - 1);
        }
    }

    private Graph addGraph(int x, int y, int hsz, int vsz) {
        Graph g = new Graph();
        g.setLocation(x, y);
        g.setSize(hsz, vsz);
        g.setBgColor(ANSI.CYAN);
        g.setFgColor(ANSI.BLACK);
        add(g);
        return g;
    }

    private Label addLabel(String text, int x, int y) {
        Label l = new Label(text);
        l.setLocation(x, y);
        l.setSize(10, 1);
        l.setJustification(Label.RIGHT);
        l.setBgColor(ANSI.CYAN);
        l.setFgColor(ANSI.BLACK);
        add(l);
        return l;
    }

    private void update() {
        Stats apps = state.getStats("apps"), jobs = state.getStats("jobs"), fops = state
            .getStats("fops"), transfers = state.getStats("transfers");
        totals[0].setText(String.valueOf(apps.getTotal()));
        int j = jobs.getTotal();
        int o = fops.getTotal();
        int t = transfers.getTotal();
        totals[1].setText(String.valueOf(j + o + t));
        totals[2].setText(String.valueOf(j));
        totals[3].setText(String.valueOf(t));
        totals[4].setText(String.valueOf(o));

        currents[0].setText(String.valueOf(apps.getCurrent()));
        j = jobs.getCurrent();
        o = fops.getCurrent();
        t = transfers.getCurrent();
        currents[1].setText(String.valueOf(j + o + t));
        currents[2].setText(String.valueOf(j));
        currents[3].setText(String.valueOf(t));
        currents[4].setText(String.valueOf(o));
        cg[0].push(apps.getCurrent());
        cg[1].push(j + o + t);
        cg[2].push(j);
        cg[3].push(t);
        cg[4].push(o);

        periods[0].setText(String.valueOf(apps.getPeriod()));
        j = jobs.getPeriod();
        o = fops.getPeriod();
        t = transfers.getPeriod();
        periods[1].setText(String.valueOf(j + o + t));
        periods[2].setText(String.valueOf(j));
        periods[3].setText(String.valueOf(t));
        periods[4].setText(String.valueOf(o));
        pg[0].push(apps.getPeriod());
        pg[1].push(j + o + t);
        pg[2].push(j);
        pg[3].push(t);
        pg[4].push(o);
        redraw();
    }

    protected void draw(ANSIContext context) throws IOException {
        context.filledRect(sx, sy, width, height);
    }

}
