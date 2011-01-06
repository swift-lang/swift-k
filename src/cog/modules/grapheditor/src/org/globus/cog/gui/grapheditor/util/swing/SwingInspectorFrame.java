
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 24, 2004
 */
package org.globus.cog.gui.grapheditor.util.swing;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;

public class SwingInspectorFrame extends JFrame{

	public SwingInspectorFrame(Component root) {
		super();
		this.getRootPane().setLayout(new BorderLayout());
		setTitle("Swing inspector");
		setSize(200, 400);
		this.getRootPane().add(new JScrollPane(new JTree(new SwingInspectorTreeModel(root))));
		show();
	}
}
