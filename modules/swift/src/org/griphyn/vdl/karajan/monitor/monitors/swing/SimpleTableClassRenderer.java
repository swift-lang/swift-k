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
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.swing;

import java.util.List;
import java.util.TimerTask;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.griphyn.vdl.karajan.monitor.StatefulItemClassSet;
import org.griphyn.vdl.karajan.monitor.common.GlobalTimer;
import org.griphyn.vdl.karajan.monitor.common.StatefulItemModel;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.SafeTimerTask;

public class SimpleTableClassRenderer<T extends StatefulItem> extends JScrollPane implements ClassRenderer {
    private JTable table;
    private String name;
    private StatefulItemClassSet<T> items;

	public SimpleTableClassRenderer(String name, StatefulItemClassSet<T> itemClassSet) {
        super(new JTable());
        this.table = (JTable) super.getViewport().getView();
        this.name = name;
        this.items = itemClassSet;
        this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        table.setModel(new Model<T>(items));
	}
    
    public AbstractTableModel getTableModel() {
    	return (AbstractTableModel) table.getModel();
    }
    
    public void setTableModel(AbstractTableModel model) {
    	table.setModel(model);
    }
    
    public void dataChanged() {
        getTableModel().fireTableDataChanged();
    }
    
    public static class Model<T extends StatefulItem> extends AbstractTableModel implements StatefulItemModel {
    	public static final int DEFAULT_UPDATE_INTERVAL = 4000;
    	
    	private int updateInterval;
    	private StatefulItemClassSet<T> items;
    	private List<T> snapShot;
    	private TimerTask ta;
    	
    	public Model(StatefulItemClassSet<T> items) {
    		this(items, DEFAULT_UPDATE_INTERVAL);
    	}
        
        public Model(StatefulItemClassSet<T> items, int updateInterval) {
            this.items = items;
            update();
            GlobalTimer.getTimer().schedule(ta = new SafeTimerTask() {
				public void runTask() {
					update();
				}}, updateInterval, updateInterval);
        } 
        
        private void update() {
        	snapShot = items.getAll();
        	fireTableDataChanged();
        }
        
		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return snapShot.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {           
			return snapShot.get(rowIndex);
		}
		
		public StatefulItem getItem(int rowIndex) {
			return snapShot.get(rowIndex);
		}
		
		public String getDetails(int rowIndex) {
			return String.valueOf(getItem(rowIndex));
		}

		public void fireTableDataChanged() {
			super.fireTableDataChanged();
		}
		
		public void stop() {
			ta.cancel();
		}
    }
}
