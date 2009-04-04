//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 27, 2009
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

public class DefaultTableCellRenderer implements TableCellRenderer {
    private Label label;
    
    public DefaultTableCellRenderer() {
        label = new Label();
    }

    public Component getComponent(Table table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
            setColors(table, isSelected, hasFocus);
            label.setText(String.valueOf(value));
            return label;
    }
    
    private void setColors(Table table, boolean selected, boolean hasFocus) {
        if (!selected) {
            label.setBgColor(table.getBgColor());
            label.setFgColor(table.getFgColor());
        }
        else {
            if (hasFocus) {
                label.setBgColor(table.getFocusedBgColor());
                label.setFgColor(table.getFgColor());
            }
            else {
                label.setBgColor(table.getHighlightBgColor());
                label.setFgColor(table.getHighlightFgColor());
            }
        }
    }

}
