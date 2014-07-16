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

import java.util.HashMap;
import java.util.Map;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.SystemStateListener;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSI;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Component;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.DefaultTableCellRenderer;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Label;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Table;

public class BensCellRenderer extends DefaultTableCellRenderer implements
        SystemStateListener {
    private Map<String, Long> lastUpdated;

    public BensCellRenderer(SystemState state) {
        lastUpdated = new HashMap<String, Long>();
        state.addListener(this);
    }

    public Component getComponent(Table table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        Label l = (Label) super.getComponent(table, value, isSelected,
            hasFocus, row, column);
        if (column == 0) {
            l.setJustification(Label.RIGHT);
        }
        else {
            l.setJustification(Label.LEFT);
        }
        long now = System.currentTimeMillis();
        Long then = lastUpdated.get(String.valueOf(row + 1));
        if (then != null) {
            long t = then.longValue();
            if (now - t < 2000) {
                l.setBgColor(ANSI.GREEN);
            }
            else if (now - t < 10000) {
                l.setBgColor(ANSI.YELLOW);
            }
            else {
                l.setBgColor(ANSI.CYAN);
            }
        }
        else {
            l.setBgColor(ANSI.CYAN);
        }
        return l;
    }

    public void itemUpdated(SystemStateListener.UpdateType updateType, StatefulItem item) {
        if (item.getItemClass().equals(StatefulItemClass.TRACE)) {
            lastUpdated.put(item.getID(), new Long(System.currentTimeMillis()));
        }
    }
}
