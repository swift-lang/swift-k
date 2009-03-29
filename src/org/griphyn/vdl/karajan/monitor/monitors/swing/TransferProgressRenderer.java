/*
 * Created on Jan 30, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.swing;

import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class TransferProgressRenderer implements TableCellRenderer {
	private JProgressBar pbar;
	
	public TransferProgressRenderer() {
		pbar = new JProgressBar();
		pbar.setMaximum(100);
		pbar.setStringPainted(true);
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		TransferProgress tp = (TransferProgress) value;
		if (tp.getTotal() == -1) {
			pbar.setEnabled(false);
			pbar.setString("N/A");
		}
		else {
			int scaled = (int) (tp.getCurrent()*100/tp.getTotal());
			pbar.setEnabled(true);
			pbar.setValue(scaled);
			pbar.setString(scaled + "%");
		}
		return pbar;
	}
}
