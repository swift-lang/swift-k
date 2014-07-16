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

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.griphyn.vdl.karajan.monitor.StatefulItemClassSet;
import org.griphyn.vdl.karajan.monitor.items.HostItem;

public class HostTableModel implements TableModel {
    private StatefulItemClassSet set;

    public HostTableModel(StatefulItemClassSet set) {
        this.set = set;
    }

    public void addTableModelListener(TableModelListener l) {
    }

    public Class getColumnClass(int columnIndex) {
        return String.class;
    }

    public int getColumnCount() {
        return 5;
    }

    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Name";
            case 1:
                return "Running Tasks";
            case 2:
                return "Max Tasks";
            case 3:
                return "Overloaded";
            case 4:
                return "Score";
            default:
                return "?";
        }
    }

    public int getRowCount() {
        return set.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        HostItem hi = (HostItem) set.getAll().get(rowIndex);
        switch (columnIndex) {
            case 0:
                return hi.getName();
            case 1:
                return hi.getJobsRunning();
            case 2:
                return hi.getJobsAllowed();
            case 3:
                return hi.getOverload();
            case 4:
                return hi.getScore();
            default:
                return "?";
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void removeTableModelListener(TableModelListener l) {
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
    }
}
