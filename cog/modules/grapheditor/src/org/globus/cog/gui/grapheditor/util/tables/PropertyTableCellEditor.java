
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.util.tables;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.gui.grapheditor.util.celleditors.BooleanEditor;
import org.globus.cog.gui.grapheditor.util.celleditors.DoubleEditor;
import org.globus.cog.gui.grapheditor.util.celleditors.Editor;
import org.globus.cog.gui.grapheditor.util.celleditors.FloatEditor;
import org.globus.cog.gui.grapheditor.util.celleditors.IntegerEditor;
import org.globus.cog.gui.grapheditor.util.celleditors.StringEditor;

/**
 * This class implements an editor for graph component properties
 */
public class PropertyTableCellEditor extends AbstractCellEditor implements TableCellEditor {
	private static Logger logger = Logger.getLogger(PropertyTableCellEditor.class);

	private Editor cellEditor;
	private GraphComponent nodeComponent;
	private Property property;

	public Component getTableCellEditorComponent(
		JTable table,
		Object value,
		boolean isSelected,
		int row,
		int column) {
		if (!(table instanceof PropertyTable)) {
			throw new RuntimeException("Cannot use a PropertyTableCellEditor with a non PropertyTable.");
		}
		PropertiesTableModel tableModel = (PropertiesTableModel) table.getModel();
		this.nodeComponent = tableModel.getGraphComponent();
		this.property = this.nodeComponent.getProperty(tableModel.getProperty(row).getName());
		Class cls = this.property.getPropertyClass();
		if (cls == null) {
			if (value == null) {
				cls = String.class;
			}
			else {
				cls = value.getClass();
			}
		}
		if (value instanceof Component) {
			return (Component) value;
		}
		if (cls.isAssignableFrom(String.class)) {
			this.cellEditor = new StringEditor();
			if (value == null) {
				value = "";
			}
			this.cellEditor.setValue(value);
		}
		else if (cls.isAssignableFrom(Integer.class)) {
			this.cellEditor = new IntegerEditor();
			this.cellEditor.setValue(value);
		}
		else if (cls.isAssignableFrom(Float.class)) {
			this.cellEditor = new FloatEditor();
			this.cellEditor.setValue(value);
		}
		else if (cls.isAssignableFrom(Double.class)) {
			this.cellEditor = new DoubleEditor();
			this.cellEditor.setValue(value);
		}
		else if (cls.isAssignableFrom(Boolean.class)) {
			this.cellEditor = new BooleanEditor();
			this.cellEditor.setValue(value);
		}
		else {
			this.cellEditor = new StringEditor();
			if (value != null) {
				this.cellEditor.setValue(value.toString());
			}
			else {
				this.cellEditor.setValue("");
			}
		}
		logger.debug(
			"Cell editor: value class="
				+ value.getClass()
				+ ", editor class="
				+ this.cellEditor.getClass());
		return (Component) this.cellEditor;
	}

	public Object getCellEditorValue() {
		this.property.setValue(this.cellEditor.getValue());
		return this.cellEditor.getValue();
	}
}
