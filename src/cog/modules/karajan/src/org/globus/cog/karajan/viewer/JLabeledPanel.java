
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 30, 2003
 */
package org.globus.cog.karajan.viewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.MenuComponent;
import java.awt.PopupMenu;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class JLabeledPanel extends JPanel {
	private static final long serialVersionUID = 6824999318032656993L;
	
	private static Font labelFont;
	private JPanel contentPane;
	private JLabel label;

	public JLabeledPanel() {
		super.setLayout(new BorderLayout());
		contentPane = new JPanel();
		label = new JLabel();
		if (labelFont == null) {
			Font oldFont = label.getFont(); 
			labelFont = new Font(oldFont.getFontName(), Font.BOLD, oldFont.getSize());
		}
		label.setFont(labelFont);
		Dimension d = label.getPreferredSize();
		d.setSize(d.width, d.height+20);
		label.setPreferredSize(d);
		label.setAlignmentY((float)0.9);
		JPanel spacer = new JPanel();
		spacer.setPreferredSize(new Dimension(8,8));
		super.add(spacer, BorderLayout.WEST);
		super.add(label, BorderLayout.NORTH);
		super.add(contentPane, BorderLayout.CENTER);
	}

	public JLabeledPanel(String title) {
		this();
		label.setText(title);
	}

	public JLabeledPanel(LayoutManager layout) {
		this();
		setLayout(layout);
	}

	public void setLabel(String label) {
		this.label.setText(label);
	}

	public Component add(Component comp) {
		return contentPane.add(comp);
	}

	public Component add(Component comp, int index) {
		return contentPane.add(comp, index);
	}

	public void add(Component comp, Object constraints) {
		contentPane.add(comp, constraints);
	}

	public void add(Component comp, Object constraints, int index) {
		contentPane.add(comp, constraints, index);
	}

	public void add(PopupMenu popup) {
		contentPane.add(popup);
	}

	public Component add(String name, Component comp) {
		return contentPane.add(name, comp);
	}

	public LayoutManager getLayout() {
		return contentPane.getLayout();
	}

	public void remove(int index) {
		contentPane.remove(index);
	}

	public void remove(Component comp) {
		contentPane.remove(comp);
	}

	public void remove(MenuComponent comp) {
		contentPane.remove(comp);
	}

	public void removeAll() {
		contentPane.removeAll();
	}

	public void setLayout(LayoutManager mgr) {
		if (contentPane != null) {
			contentPane.setLayout(mgr);
		}
		else {
			return;
		}
	}
	
	public JComponent getContentPane() {
		return contentPane;
	}

}
