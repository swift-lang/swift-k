
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.mimehandler;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import java.util.Enumeration;
import java.util.Hashtable;

public class MimeHandlerEditor extends JPanel{
	private Hashtable mimeHash;
	
	public MimeHandlerEditor(MimeHandler mimeHandler) {
		super();
		this.mimeHash = mimeHandler.getHash();
		JTable mimeTable = new JTable(createTableModel());
	    JScrollPane mainScrollpane = new JScrollPane(mimeTable);
	    this.add(mainScrollpane);
	}
	
	private Object[][] createObjectArray() {
		Object[][] tableModelData = new Object[2][mimeHash.size()];
		Enumeration keys = mimeHash.keys();
		int i = 0;
		while(keys.hasMoreElements()) {
			tableModelData[0][i] = keys.nextElement();
			tableModelData[1][i] = mimeHash.get(tableModelData[0][i]);
			i++;
		}
	return tableModelData;		
	}
	
	private TableModel createTableModel() {
		TableModel dataModel = new AbstractTableModel() {
			Object[][] tableModelData = createObjectArray();
	        public int getColumnCount() { return 2; }
	        public int getRowCount() { return mimeHash.size();}
	        public Object getValueAt(int row, int col) { return tableModelData[col][row]; }
		};
	    return dataModel;
	}
	
}
