
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.util.tables;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.globus.cog.gui.grapheditor.nodes.NodeComponent;

/**
 * This class extends the Swing table to include a custom
 * model/renderer/editor
 */
public class ExtendedTable extends JTable {

    TableCellRenderer renderer;
    TableCellEditor editor;

    public ExtendedTable(TableModel td) {
        super(td);
        renderer = new ExtendedTableCellRenderer();
        editor = new ExtendedTableCellEditor();
        setDefaultRenderer(Object.class, renderer);
        setDefaultEditor(Object.class, editor);
    }

	public void repaintNode(NodeComponent component) {
		if (getModel() instanceof IntrospectiveTableModel) {
			((IntrospectiveTableModel) getModel()).repaintNode(component);
		}
	}

    /*public TableCellRenderer getCellRenderer(int row, int col){
        return renderer;
    }*/
}
