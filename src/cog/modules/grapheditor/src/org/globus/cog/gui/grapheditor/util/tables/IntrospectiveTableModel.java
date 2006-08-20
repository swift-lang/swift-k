// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.grapheditor.util.tables;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.properties.Introspector;
import org.globus.cog.gui.grapheditor.properties.Property;

/**
 * A table model that displays components and their properties. The collumns are
 * automatically generated based on the common properties of the contained
 * objects. If there are no common properties, nothing will be displayed.
 */
public class IntrospectiveTableModel extends AbstractTableModel implements TableModel,
		PropertyChangeListener {

	private List objects;
	private List properties;
	private LinkedHashMap columns;
	private List columnsInt;
	private Hashtable nameMap;

	public IntrospectiveTableModel(Collection objects) {
		super();
		setObjects(objects);
	}

	public IntrospectiveTableModel() {
		super();
	}

	public void setObjects(Collection objects) {
		if (this.objects != null) {
			Iterator i = this.objects.listIterator();
			while (i.hasNext()) {
				((NodeComponent) i.next()).removePropertyChangeListener(this);
			}
		}
		if (objects == null) {
			this.objects = null;
			columns = null;
			nameMap = null;
			return;
		}
		this.objects = new ArrayList(objects);
		if (this.objects != null) {
			Iterator i = this.objects.listIterator();
			while (i.hasNext()) {
				NodeComponent nc = (NodeComponent) i.next();
				nc.addPropertyChangeListener(this);
			}
		}
		properties = new ArrayList(Introspector.getCommonProperties(objects));

		columns = new LinkedHashMap();
		nameMap = new Hashtable();
		columns.put("Node", Boolean.TRUE);
		nameMap.put("Node", "");
		Iterator i = properties.iterator();
		while (i.hasNext()) {
			Property prop = (Property) i.next();
			String name = capitalize(prop.getName());
			nameMap.put(name, prop.getName());
			columns.put(name, Boolean.TRUE);
		}
		updateColumnList();

		fireTableStructureChanged();
	}

	protected void updateColumnList() {
		columnsInt = new LinkedList();
		Iterator i = columns.keySet().iterator();
		while (i.hasNext()) {
			String cname = (String) i.next();
			if (Boolean.TRUE.equals(columns.get(cname))) {
				columnsInt.add(cname);
			}
		}
	}

	public int getRowCount() {
		if (objects == null) {
			return 0;
		}
		return objects.size();
	}

	public int getColumnCount() {
		if (columnsInt != null) {
			return columnsInt.size();
		}
		return 0;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if ((rowIndex < 0) || (rowIndex >= objects.size())) {
			return null;
		}
		String name = (String) nameMap.get(columnsInt.get(columnIndex));
		if (name == null) {
			throw new IllegalArgumentException("Invalid column table: " + columnsInt.toString());
		}
		if (name.equals("")) {
			return objects.get(rowIndex);
		}
		else {
			NodeComponent o = (NodeComponent) objects.get(rowIndex);
			return o.getPropertyValue(name);
		}
	}

	public String getColumnName(int columnIndex) {
		return (String) columnsInt.get(columnIndex);
	}

	private String capitalize(String orig) {
		return orig.substring(0, 1).toUpperCase() + orig.substring(1);
	}

	public Class getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return Component.class;
		}
		Property property = (Property) properties.get(columnIndex - 1);
		if ((getRowCount() > 0) && (objects != null)) {
			return property.getClass();
		}
		return Object.class;
	}

	public boolean isCellEditable(int row, int col) {
		if (col == 0) {
			return true;
		}
		Property property = (Property) properties.get(col - 1);
		return (property.isWritable() || property.isInteractive());
	}

	public void propertyChange(PropertyChangeEvent evt) {
		int i = objects.indexOf(evt.getSource());
		fireTableRowsUpdated(i, i);
	}

	public void repaintNode(NodeComponent component) {
		int count = 0;
		Iterator i = objects.iterator();
		while (i.hasNext()) {
			Object o = i.next();
			if (o == component) {
				fireTableCellUpdated(count, 0);
				break;
			}
			count++;
		}
	}

	public LinkedHashMap getColumns() {
		return this.columns;
	}

	public void setColumns(LinkedHashMap columns) {
		this.columns = columns;
		updateColumnList();
		fireTableStructureChanged();
	}
}