
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 19, 2004
 */
package org.globus.cog.gui.grapheditor.util.tree;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.targets.swing.views.TreeView;
import org.globus.cog.util.ImageLoader;

public class TreeNodeRenderer implements TreeCellRenderer {

	private Container expandedContainer, collapsedContainer;
	private JLabel expanded, collapsed, string;
	private TreeView view;

	public TreeNodeRenderer(TreeView view) {
		this.view = view;
		expanded = new JLabel();
		expanded.setIcon(ImageLoader.loadIcon("images/node-expanded.png"));
		collapsed = new JLabel();
		collapsed.setIcon(ImageLoader.loadIcon("images/node-collapsed.png"));
		string = new JLabel();
		expandedContainer = new Container();
		expandedContainer.setLayout(new FlowLayout());
		expandedContainer.add(expanded);
		collapsedContainer = new Container();
		collapsedContainer.setLayout(new FlowLayout());
		collapsedContainer.add(collapsed);
	}
	
	public Component getTreeCellRendererComponent(
		JTree tree,
		Object value,
		boolean selected,
		boolean expanded,
		boolean leaf,
		int row,
		boolean hasFocus) {

		Component component = null;

		if (value instanceof NodeComponent) {
			component = view.getRenderer((NodeComponent) value).getVisualComponent();
		}
		else {
			component = string;
			string.setText(value.toString());
			string.setIcon(ImageLoader.loadIcon("images/tree-node.png"));
		}
		
		if (leaf) {
			return component;
		}
		
		Container container = null;

		if (expanded) {
			container = expandedContainer; 
		}
		else {
			container = collapsedContainer;
		}
		if (container.getComponentCount() == 2) {
			container.remove(1);
			container.add(component);
		}
		else {
			container.add(component);
		}
		
		container.validate();
		
		return component;
	}
}
