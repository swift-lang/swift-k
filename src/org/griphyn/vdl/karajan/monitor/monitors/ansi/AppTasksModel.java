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

import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.items.Bridge;
import org.griphyn.vdl.karajan.monitor.items.TaskItem;

public class AppTasksModel implements TableModel {
    private ApplicationItem app;

    public AppTasksModel(ApplicationItem app) {
        this.app = app;
    }

    public void addTableModelListener(TableModelListener l) {
    }

    public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            default:
                return Object.class;
        }
    }

    public int getColumnCount() {
        return 3;
    }

    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Type";
            case 1:
                return "Details";
            case 2:
                return "State";
            default:
                return "?";
        }
    }

    public int getRowCount() {
        return app.getChildren().size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Bridge b = (Bridge) app.getChildren().toArray()[rowIndex];
        TaskItem ti = (TaskItem) b.getChildren().iterator().next();
        switch (columnIndex) {
            case 0:
                return getTaskType(ti.getTask());
            case 1:
                return ti.getTask();
            case 2:
                return getStatus(ti.getTask());
            default:
                return "?";
        }
    }

    private String getTaskType(Task t) {
        switch (t.getType()) {
            case Task.JOB_SUBMISSION:
                return "Job";
            case Task.FILE_TRANSFER:
                return "Transfer";
            case Task.FILE_OPERATION:
                return "Fileop";
            default:
                return "?";
        }
    }

    private String getStatus(Task t) {
        switch (t.getStatus().getStatusCode()) {
            case Status.ACTIVE:
                return "R";
            case Status.COMPLETED:
                return "C";
            case Status.FAILED:
                return "F";
            case Status.CANCELED:
                return "X";
            case Status.SUBMITTED:
                return "Q";
            case Status.UNSUBMITTED:
                return "N";
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
