
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 4, 2004
 *
 */
package org.globus.cog.gui.grapheditor.targets.swing.util.export;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.globus.cog.gui.grapheditor.canvas.views.layouts.ExtendedSpringLayout;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.FlowLayout;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.HierarchicalLayout;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.RadialFlowLayout;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.RadialHierarchicalLayout;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.RadialLayout;

public abstract class LayoutedExportPanel extends ExportPanel {
	private LinkedHashMap layouts;
	private JComboBox layoutsCombo;
	
	public LayoutedExportPanel() {
		layouts = new LinkedHashMap();
		layouts.put("Keep Existing Layout", PassiveLayout.class);
		layouts.put("Hierarchical Layout", HierarchicalLayout.class);
		layouts.put("Radial Layout", RadialLayout.class);
		layouts.put("Radial Hierarchical Layout", RadialHierarchicalLayout.class);
		layouts.put("Flow Layout", FlowLayout.class);
		layouts.put("Radial Flow Layout", RadialFlowLayout.class);
		layouts.put("Spring Layout", ExtendedSpringLayout.class);
	}
	
	public void setup() {
		setLayout(new BorderLayout());
		JLabel label = new JLabel("Layout Algorithm: ");
		Container c0 = new Container();
		c0.setLayout(new GridLayout(2, 1));
		Container c1 = new Container();
		c1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
		c1.add(label);
		layoutsCombo = new JComboBox();
		Iterator i = layouts.keySet().iterator();
		while (i.hasNext()) {
			layoutsCombo.addItem(i.next());
		}
		c1.add(layoutsCombo);
		c0.add(c1);
		add(c0, BorderLayout.NORTH);
	}
	
	public Class getLayoutEngine() {
		return (Class) layouts.get(layoutsCombo.getSelectedItem());
	}
}
