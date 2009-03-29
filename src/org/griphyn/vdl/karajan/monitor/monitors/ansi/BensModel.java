//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 28, 2009
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
    private List lines;

    public BensModel(SystemState state) {
        this.state = state;
        lines = new ArrayList();
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

    public Class getColumnClass(int columnIndex) {
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

    public void itemUpdated(int updateType, StatefulItem item) {
        if (item.getItemClass().equals(StatefulItemClass.TRACE)) {
            fireTableDataChanged();
        }
    }

}
