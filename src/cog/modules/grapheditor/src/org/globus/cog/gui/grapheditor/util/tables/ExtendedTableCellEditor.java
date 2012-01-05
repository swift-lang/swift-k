
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.util.tables;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.targets.swing.util.ListComponentWrapper;
import org.globus.cog.gui.grapheditor.targets.swing.util.NodeComponentWrapper;

/**
 * A simple cell editor which allows Swing components to edit
 * themselves. In other words, if a Swing component is found, it will
 * be its own editor. It allows components within tables to receive
 * mouse events.
 */
public class ExtendedTableCellEditor extends DefaultCellEditor implements TableCellEditor {

    private JTextField stringEditor;
	private ListComponentWrapper lcw;
	private NodeComponentWrapper w;

    public ExtendedTableCellEditor() {
        super(new JCheckBox());
        stringEditor = new JTextField();
		lcw = new ListComponentWrapper();
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof Component) {
			lcw.setComponent((Component) value);
            return lcw;
        }
        if (value instanceof String) {
            stringEditor.setText((String) value);
            return stringEditor;
        }
        else if (value instanceof NodeComponent) {
        	if (w == null) {
        		w = new NodeComponentWrapper((NodeComponent) value);
        		w.setUsedAsRenderer(true);
        	}
        	else {
        		w.setGraphComponent((NodeComponent) value);
        	}
        	w.validate();
        	return w;
        }
        else {
        	if (value != null){
            	stringEditor.setText(value.toString());
        	}
        	else{
        		stringEditor.setText(null);
        	}
            return stringEditor;
        }
    }
}
