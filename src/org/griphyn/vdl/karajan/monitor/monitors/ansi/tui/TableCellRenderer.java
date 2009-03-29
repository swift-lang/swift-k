//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 27, 2009
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

public interface TableCellRenderer {
    Component getComponent(Table table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column);
}
