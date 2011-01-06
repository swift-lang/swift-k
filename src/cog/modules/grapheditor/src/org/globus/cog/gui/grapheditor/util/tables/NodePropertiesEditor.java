
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.util.tables;


import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.globus.cog.gui.grapheditor.GraphComponent;

/**
 * A frame that encapsulates a table that can be used to edit graph
 * component properties
 */
public class NodePropertiesEditor extends JFrame {

	private JScrollPane panel;
	private JTable table;
	private GraphComponent nodeComponent;
	private PropertiesTableModel model;

	public NodePropertiesEditor(GraphComponent nodeComponent) {
		super();
		if (nodeComponent.hasProperty("name")) {
			setTitle((String) nodeComponent.getPropertyValue("name"));
		}
		this.nodeComponent = nodeComponent;
		getContentPane().setLayout(new BorderLayout());
		model = new PropertiesTableModel(nodeComponent);
		table = new PropertyTable(model);
		panel = new JScrollPane(table);
		getContentPane().add(panel, BorderLayout.CENTER);
		table.getColumnModel().getColumn(1).setWidth(1);
		pack();
		int insetsHeight = getHeight() - getRootPane().getHeight();
		setSize(200, model.getRowCount() * table.getRowHeight()
			+ table.getTableHeader().getHeight() + insetsHeight);
	}

	/*
	 * The filter is a regexp expression. If a second capturing group exists,
	 * it will be used as the filtered name for the property
	 */
	public void setFilter(String filter) {
		this.model.setFilter(filter);
	}

	public String getFilter() {
		return this.model.getFilter();
	}

}
