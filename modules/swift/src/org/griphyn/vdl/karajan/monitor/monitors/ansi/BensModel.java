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

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.SystemStateListener;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.items.TraceItem;

public class BensModel extends AbstractTableModel implements SystemStateListener {
    private SystemState state;
    private List<String> lines;

    public BensModel(SystemState state) {
        this.state = state;
        lines = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(state.getProjectName() + ".swift"));
            String line = br.readLine();
            while (line != null) {
                lines.add(line);
                line = br.readLine();
            }
            br.close();
            state.addListener(this);
        }
        catch (Exception e) {
            lines.add(e.toString());
        }
    }

    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return "Compl./Started";
        }
        else {
            return "Swift Source";
        }
    }

    public int getRowCount() {
        return lines.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            return lines.get(rowIndex);
        }
        else {
            TraceItem ti = (TraceItem) state.getItemClassSet(
                StatefulItemClass.TRACE).getByID(String.valueOf(rowIndex + 1));
            if (ti != null) {
                return ti.getEnded() + "/" + ti.getStarted();
            }
            else {
                return "";
            }
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void removeTableModelListener(TableModelListener l) {
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
    }

    public void itemUpdated(SystemStateListener.UpdateType updateType, StatefulItem item) {
        if (item.getItemClass().equals(StatefulItemClass.TRACE)) {
            fireTableDataChanged();
        }
    }

}
