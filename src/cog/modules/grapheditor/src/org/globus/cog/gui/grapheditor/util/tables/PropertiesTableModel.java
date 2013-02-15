
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.util.tables;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.properties.Introspector;
import org.globus.cog.gui.grapheditor.properties.Property;

/**
 * A table model that can be used to display all the properties of a graph component and
 * their values.
 */

public class PropertiesTableModel extends AbstractTableModel implements TableModel, PropertyChangeListener {

	private static Logger logger = Logger.getLogger(PropertiesTableModel.class);
	private List properties = null;
	private HashMap filteredNames;
	private GraphComponent nodeComponent;
	private String filter;

	public PropertiesTableModel(GraphComponent nodeComponent) {
		super();
		setObject(nodeComponent);
	}

	public PropertiesTableModel() {
		super();
	}

	public void setFilter(String filter) {
		logger.debug("Setting filter to " + filter);
		this.filter = filter;
		updatePropertyList();
	}

	public String getFilter() {
		return this.filter;
	}

	public void setObject(GraphComponent nodeComponent) {
		this.nodeComponent = nodeComponent;
		updatePropertyList();
	}

	protected void updatePropertyList() {
		if (this.nodeComponent != null) {
			this.filteredNames = new HashMap();
			this.nodeComponent.addPropertyChangeListener(this);
			ArrayList tmpprops = new ArrayList(Introspector.getProperties(this.nodeComponent));
			this.properties = new ArrayList();
			Iterator i = tmpprops.listIterator();
			while (i.hasNext()) {
				Property prop = (Property) i.next();
				if (!prop.hasAccess(Property.HIDDEN)) {
					if (this.filter == null) {
						this.properties.add(prop);
					}
					else {
						Pattern pattern = Pattern.compile(this.filter);
						Matcher matcher = pattern.matcher(prop.getName());
						if (matcher.matches()) {
							String group = null;

							if (matcher.groupCount() > 0) {
								group = matcher.group(1);
							}
							if (group == null) {
								group = matcher.group(0);
							}								
							this.filteredNames.put(prop, group);
							this.properties.add(prop);
						}
					}
				}
			}
		}
		else {
			this.properties = null;
		}
		fireTableStructureChanged();
	}

	public int getRowCount() {
		if (this.properties == null) {
			return 0;
		}
		return this.properties.size();
	}

	public int getColumnCount() {
		return 2;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Property property = (Property) this.properties.get(rowIndex);
		if (columnIndex == 0) {
			if (this.filteredNames.containsKey(property)) {
				return (String) this.filteredNames.get(property);
			}
			return property.getDisplayName();
		}
		else {
			return property.getValue();
		}
	}

	public String getColumnName(int columnIndex) {
		if (columnIndex == 0) {
			return "Property";
		}
		else {
			return "Value";
		}
	}

	public Class getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return String.class;
		}
		else {
			return Object.class;
		}
	}

	public boolean isCellEditable(int row, int col) {
		if (col == 0) {
			return false;
		}
		Property prop = (Property) this.properties.get(row);
		return prop.isWritable();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		int i = this.properties.indexOf(evt.getPropertyName());
		fireTableRowsUpdated(i, i);
	}

	public GraphComponent getGraphComponent() {
		return this.nodeComponent;
	}

	public Property getProperty(int row) {
		return (Property) this.properties.get(row);
	}
}
