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
