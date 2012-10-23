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
