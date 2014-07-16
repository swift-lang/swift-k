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

import java.util.HashMap;
import java.util.Map;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class Table extends Container implements TableModelListener {
	private TableModel model;
	private Map<Integer, Integer> colWidths;
	private int presetColWidthsTotal;
	private int presetColWidthsCount;
	private int firstRow;
	private int selectedRow;
	private VScrollbar sb;
	private TableCellRenderer cellRenderer;

	public Table() {
	    this(new DefaultTableModel());
	}
	
	public Table(TableModel model) {
		this.model = model;
		colWidths = new HashMap<Integer, Integer>();
		sb = new VScrollbar();
		sb.setThumbChar(ANSI.GCH_BULLET);
		cellRenderer = new DefaultTableCellRenderer();
	}

	public TableModel getModel() {
		return model;
	}

	public void setModel(TableModel model) {
		if (this.model != null) {
			this.model.removeTableModelListener(this);
		}
		this.model = model;
		if (this.model != null) {
			this.model.addTableModelListener(this);
		}
		invalidate();
	}

	public void setColumnWidth(int index, int width) {
		Integer old = colWidths.put(index, width);
		if (old != null) {
			presetColWidthsTotal -= old;
			presetColWidthsCount--;
		}
		presetColWidthsTotal += width;
		if (width != 0) {
			presetColWidthsCount++;
		}
	}

	protected void validate() {
		if (isValid()) {
			return;
		}
		removeAll();
		sb.setLocation(width, 2);
		sb.setSize(1, height - 2);
		sb.setBgColor(bgColor);
		sb.setFgColor(fgColor);
		int cc = model.getColumnCount();
		if (cc == 0) {
			return;
		}
		float defaultWidth;
		if (cc == presetColWidthsCount) {
			defaultWidth = 0;
		}
		else {
			defaultWidth = ((float) (width - presetColWidthsTotal)) / (cc - presetColWidthsCount);
		}
		float cx = 0;
		for (int i = 0; i < cc; i++) {
			boolean last = i == cc - 1;
			Integer presetColWidth = colWidths.get(i);
			int colWidth;
			if (presetColWidth == null) {
				colWidth = (int) ((cx + defaultWidth) - (int) cx);
			}
			else {
				colWidth = presetColWidth.intValue();
			}
			if (last) {
				colWidth++;
			}
			TableColumn c = new TableColumn(this, model, i);
			c.setBgColor(bgColor);
			c.setFgColor(fgColor);
			c.setLocation((int) cx, 0);
			if (last) {
			    c.setSize(width - (int) cx, height);
			}
			else {
			    c.setSize(colWidth - 1, height);
			}
			add(c);

			if (!last) {
				VLine l = new VLine(true);
				l.setBgColor(bgColor);
				l.setFgColor(fgColor);
				l.setLocation((int) (cx + colWidth) - 1, 0);
				l.setSize(1, height);
				add(l);

				VHCrossing cr = new VHCrossing();
				cr.setBgColor(bgColor);
				cr.setFgColor(fgColor);
				cr.setLocation((int) (cx + colWidth) - 1, 1);
				add(cr);
			}

			cx += colWidth;
		}
		add(sb);
		super.validate();
	}

	public void tableChanged(TableModelEvent e) {
		if (e.getType() == TableModelEvent.DELETE || e.getType() == TableModelEvent.INSERT) {
			invalidate();
		}
		else {
			redraw();
		}
	}

	public void dataChanged() {
		tableChanged(new TableModelEvent(model));
	}

	public int getFirstRow() {
		return firstRow;
	}

	public void setFirstRow(int firstRow) {
		this.firstRow = firstRow;
	}

	public int getSelectedRow() {
		return selectedRow;
	}

	public void setSelectedRow(int selectedRow) {
		if (this.selectedRow != selectedRow) {
			sb.setTotal(getModel().getRowCount());
			sb.setCurrent(selectedRow);
			this.selectedRow = selectedRow;
			for (Component c : components) {
				if (c instanceof TableColumn) {
					((TableColumn) c).setSelectedRow(selectedRow);
				}
			}
			if (selectedRow < firstRow) {
			    firstRow = Math.max(selectedRow - height + 4, 0);
			}
			if (selectedRow > firstRow + height - 4) {
			    firstRow = selectedRow;
			}
			redraw();
		}
	}

	public boolean keyboardEvent(Key key) {
		if (key.getModifiers() == 0) {
			int sr = selectedRow;
			if (key.getKey() == Key.DOWN) {
				sr++;
			}
			else if (key.getKey() == Key.UP) {
				sr--;
			}
			else if (key.getKey() == Key.PGUP) {
				sr -= height - 4;
			}
			else if (key.getKey() == Key.PGDN) {
				sr += height - 4;
			}
			else {
				return super.keyboardEvent(key);
			}
			if (sr < 0) {
				sr = 0;
			}
			if (sr > model.getRowCount() - 1) {
				sr = model.getRowCount() - 1;
			}
			setSelectedRow(sr);
			return true;
		}
		else {
			return super.keyboardEvent(key);
		}
	}
	
    public TableCellRenderer getCellRenderer() {
        return cellRenderer;
    }

    public void setCellRenderer(TableCellRenderer cellRenderer) {
        this.cellRenderer = cellRenderer;
    }

    public int getHighlightFgColor() {
        return bgColor;
    }

    public int getHighlightBgColor() {
        return fgColor;
    }

    public boolean isFocusable() {
        return true;
    }

    
    public boolean focusFirst() {
        focus();
        return true;
    }

    public boolean focusNext() {
        return false;
    }
}
