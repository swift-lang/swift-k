
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Feb 19, 2004
 *
 */
package org.globus.cog.gui.grapheditor.util.tree;

import java.awt.Component;

import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;

import org.globus.cog.gui.grapheditor.targets.swing.util.ListComponentWrapper;

public class TreeNodeEditor extends DefaultTreeCellEditor {

	private ListComponentWrapper lcw;
	private JTextField stringEditor;
	
	public TreeNodeEditor(JTree tree) {
		super(tree, null, null);
		stringEditor = new JTextField();
		lcw = new ListComponentWrapper();
	}

	public Component getTreeCellEditorComponent(
		JTree tree,
		Object value,
		boolean isSelected,
		boolean expanded,
		boolean leaf,
		int row) {
		if (value instanceof Component) {
			lcw.setComponent((Component) value);
			return lcw;
		}
		if (value instanceof String) {
			stringEditor.setText((String) value);
			return stringEditor;
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
