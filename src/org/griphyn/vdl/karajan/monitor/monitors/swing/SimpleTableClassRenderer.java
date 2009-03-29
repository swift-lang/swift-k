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
import org.griphyn.vdl.karajan.monitor.common.StatefulItemModel;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.GlobalTimer;

public class SimpleTableClassRenderer extends JScrollPane implements ClassRenderer {
    private JTable table;
    private String name;
    private StatefulItemClassSet items;

	public SimpleTableClassRenderer(String name, StatefulItemClassSet itemClassSet) {
        super(new JTable());
        this.table = (JTable) super.getViewport().getView();
        this.name = name;
        this.items = itemClassSet;
        this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        table.setModel(new Model(items));
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
    
    public static class Model extends AbstractTableModel implements StatefulItemModel {
    	public static final int DEFAULT_UPDATE_INTERVAL = 4000;
    	
    	private int updateInterval;
    	private StatefulItemClassSet items;
    	private List snapShot;
    	private TimerTask ta;
    	
    	public Model(StatefulItemClassSet items) {
    		this(items, DEFAULT_UPDATE_INTERVAL);
    	}
        
        public Model(StatefulItemClassSet items, int updateInterval) {
            this.items = items;
            update();
            GlobalTimer.getTimer().schedule(ta = new TimerTask() {
				public void run() {
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
			return (StatefulItem) snapShot.get(rowIndex);
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
