
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.util.tables;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * A table that uses a PropertyTableModel to display/edit the
 * properties of a graph component 
 */
public class PropertyTable extends JTable {

    TableCellRenderer renderer;
    TableCellEditor editor;

    public PropertyTable(PropertiesTableModel td) {
        super(td);
        renderer = new PropertyTableCellRenderer();
        editor = new PropertyTableCellEditor();
        for (int i = 0; i < td.getColumnCount(); i++) {
            Class columnClass = td.getColumnClass(i);
            setDefaultRenderer(columnClass, renderer);
            setDefaultEditor(columnClass, editor);
        }
    }
}
