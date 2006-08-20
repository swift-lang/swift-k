
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.util.tables;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.util.HexUtil;

/**
 * This class implements a renderer for graph component properties
 */
public class PropertyTableCellRenderer
	extends DefaultTableCellRenderer
	implements TableCellRenderer {
	private static Logger logger = Logger.getLogger(PropertyTableCellRenderer.class);
	private JLabel label;

	public PropertyTableCellRenderer() {
		this.label = new JLabel();
	}

	public Component getTableCellRendererComponent(
		JTable table,
		Object value,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column) {
		Component renderer = null;
		if (value instanceof ImageIcon) {
			this.label.setIcon((Icon) value);
			renderer = this.label;
		}
		else if (value instanceof Component) {
			renderer = (Component) value;
		}
		else if (value instanceof Color) {
			this.label.setBackground((Color) value);
			this.label.setOpaque(true);
			this.label.setToolTipText("#" + new String(HexUtil.hex24(((Color) value).getRGB())));
			this.label.setIcon(null);
			renderer = this.label;
		}
		if (renderer == null) {
			return super.getTableCellRendererComponent(
				table,
				value,
				isSelected,
				hasFocus,
				row,
				column);
		}
		else {
			return this.label;
		}
	}
}
