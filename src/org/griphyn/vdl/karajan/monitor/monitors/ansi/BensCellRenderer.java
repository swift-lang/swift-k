//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 29, 2009
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
    private Map lastUpdated;

    public BensCellRenderer(SystemState state) {
        lastUpdated = new HashMap();
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
        Long then = (Long) lastUpdated.get(String.valueOf(row + 1));
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

    public void itemUpdated(int updateType, StatefulItem item) {
        if (item.getItemClass().equals(StatefulItemClass.TRACE)) {
            lastUpdated.put(item.getID(), new Long(System.currentTimeMillis()));
        }
    }
}
