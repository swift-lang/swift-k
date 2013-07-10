
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.util.tables;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.targets.swing.util.NodeComponentWrapper;

/**
 * A renderer that allows Swing components to be used as their own
 * renderers.
 */
public class ExtendedTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {

    private JLabel label;
    private NodeComponentWrapper w;

    public ExtendedTableCellRenderer() {
        label = new JLabel();
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof ImageIcon) {
            label.setIcon((Icon) value);
            return label;
        }
        else if (value instanceof Component) {
            return (Component) value;
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
        	if (!w.isUsedAsRenderer()) {
        		System.out.println("????????????");
        	}
        	if (table.getRowHeight(row) != w.getHeight()) {
        		try {
        			table.setRowHeight(row, w.getHeight());
        		}
        		catch(Exception e) {
        			e.printStackTrace();
        		}
        	}
        	return w;
        }
        else {
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}
