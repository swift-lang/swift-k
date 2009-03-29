//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 28, 2009
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSI;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Table;

public class BensPane extends Table {
    private SystemState state;

    public BensPane(SystemState state) {
        this.state = state;
        setModel(new BensModel(state));
        setCellRenderer(new BensCellRenderer(state));
        setColumnWidth(0, 20);
        setBgColor(ANSI.CYAN);
        setFgColor(ANSI.BLACK);
    }

    public int getHighlightBgColor() {
        return ANSI.CYAN;
    }

    public int getHighlightFgColor() {
        return ANSI.BLUE;
    }
}
