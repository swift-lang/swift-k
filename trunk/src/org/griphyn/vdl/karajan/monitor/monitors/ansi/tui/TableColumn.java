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


/*
 * Created on Feb 21, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;

import javax.swing.table.TableModel;

public class TableColumn extends Container {
    private final TableModel model;
    private final int index;
    private final Table table;
    private int selectedRow;

    public TableColumn(Table table, TableModel model, int index) {
        this.table = table;
        this.model = model;
        this.index = index;
    }

    public TableModel getModel() {
        return model;
    }

    protected void draw(ANSIContext context) throws IOException {
        super.draw(context);
        int fr = table.getFirstRow();
        for (int i = 0; i < Math.min(model.getRowCount() - fr, height - 2); i++) {
            Component comp = table.getCellRenderer().getComponent(table,
                model.getValueAt(i + fr, index), i + fr == selectedRow, getParent().hasFocus(),
                i + fr, index);
            comp.setAbsoluteLocation(sx, sy + i + 2);
            comp.setSize(getWidth(), 1);
            comp.draw(context);
        }
        context.lock();
        try {
            context.bgColor(bgColor);
            for (int i = Math.min(model.getRowCount() - fr + 1, height - 1); i < height - 1; i++) {
                context.moveTo(sx, sy + i + 1);
                context.spaces(width);
            }
        }
        finally {
            context.unlock();
        }
    }

    protected void validate() {
        if (isValid()) {
            return;
        }
        removeAll();
        Label text = new Label(model.getColumnName(index));
        text.setBgColor(bgColor);
        text.setFgColor(fgColor);
        text.setLocation(0, 0);
        text.setSize(width, 1);
        add(text);

        HLine l = new HLine(index == 0, index == model.getColumnCount() - 1);
        l.setBgColor(bgColor);
        l.setFgColor(fgColor);
        l.setLocation(0, 1);
        l.setSize(width, 1);
        add(l);
        super.validate();
    }

    protected void setSelectedRow(int selectedRow) {
        if (this.selectedRow != selectedRow) {
            this.selectedRow = selectedRow;
        }
    }
}
