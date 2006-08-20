
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 16, 2004
 *
 */
package org.globus.cog.gui.grapheditor.targets.swing.util;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ColumnsEditor extends JFrame implements ActionListener, WindowListener{
	private JButton close, update;
	private LinkedHashMap cols;
	private Listener listener;
	
	public ColumnsEditor(LinkedHashMap cols, Listener listener) {
		this.cols = cols;
		this.listener = listener;
		setTitle("Columns");
		getContentPane().setLayout(new BorderLayout());
		JPanel main = new JPanel();
		main.setLayout(new GridLayout(0,1));
		Iterator i = cols.keySet().iterator();
		while (i.hasNext()) {
			String name = (String) i.next();
			JCheckBox cb = new JCheckBox(name);
			main.add(cb);
			cb.addActionListener(this);
			cb.setSelected(cols.get(name) == Boolean.TRUE);
		}
		getContentPane().add(new JLabel("Select visible columns:"), BorderLayout.NORTH);
		getContentPane().add(main, BorderLayout.CENTER);
		JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		update = new JButton("Update");
		update.setEnabled(false);
		update.addActionListener(this);
		buttons.add(update);
		close = new JButton("Close");
		close.addActionListener(this);
		buttons.add(close);
		getContentPane().add(buttons, BorderLayout.SOUTH);
		pack();
	}
	
	public void close() {
		hide();
		dispose();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JCheckBox) {
			JCheckBox cb = (JCheckBox) e.getSource();
			cols.put(cb.getText(), Boolean.valueOf(cb.isSelected()));
			update.setEnabled(true);
			return;
		}
		if (e.getSource() == update) {
			listener.columnsUpdated(this);
			update.setEnabled(false);
		}
		if (e.getSource() == close) {
			listener.editorClosed(this);
			hide();
			dispose();
		}
	}

	public LinkedHashMap getColumns() {
		return cols;
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		listener.editorClosed(this);
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
	
	public interface Listener {
		public void columnsUpdated(ColumnsEditor e);
		
		public void editorClosed(ColumnsEditor e);
	}
}
