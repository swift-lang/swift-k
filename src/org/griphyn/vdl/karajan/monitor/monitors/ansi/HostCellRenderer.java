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

import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSI;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSIContext;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Component;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.DefaultTableCellRenderer;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Label;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Table;

public class HostCellRenderer extends DefaultTableCellRenderer {

    private BarRenderer tr;
    
    public HostCellRenderer() {
        tr = new BarRenderer();
        tr.setJustification(Label.CENTER);
    }

    public Component getComponent(Table table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (column == 4) {
            tr.setValue((String) value);
            return tr;
        }
        else {
            Label l =  (Label) super.getComponent(table, value, isSelected, hasFocus, row,
                column);
            if (column == 0) {
                l.setJustification(Label.LEFT);
            }
            else {
                l.setJustification(Label.RIGHT);
            }
            return l;
        }
    }

    private class BarRenderer extends Label {
        private String value;

        public void setValue(String v) {
            this.value = v;
            super.setText(v);
        }

        protected void draw(ANSIContext context) throws IOException {
            super.draw(context);
            int w = this.getWidth();
            double v;
            try {
                v = Double.parseDouble(value);
            }
            catch (NumberFormatException e) {
                v = 0;
            }
            int nc = (int) (v * w / 100);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < nc; i++) {
                sb.append(context.getChar(sx + i, sy));
            }
            context.moveTo(sx, sy);
            context.bgColor(ANSI.BLUE);
            context.text(sb.toString());
        }
    }
}
