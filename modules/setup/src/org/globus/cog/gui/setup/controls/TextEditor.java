
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------
    
package org.globus.cog.gui.setup.controls;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.globus.cog.gui.setup.util.Callback;
import org.globus.cog.gui.util.UITools;

public class TextEditor extends JFrame implements ActionListener {
	private String initialContents;
	private JMenuItem revert, save, exit;
	private JTextArea text;
	private Callback callback;

	public TextEditor(String contents, Callback callback) {
		super();
		initialContents = contents;
		this.callback = callback;
		JMenu Menu = new JMenu("File");
		revert = new JMenuItem("Revert");
		revert.addActionListener(this);
		save = new JMenuItem("Exit and Save");
		save.addActionListener(this);
		exit = new JMenuItem("Exit");
		exit.addActionListener(this);
		Menu.add(revert);
		Menu.add(save);
		Menu.add(exit);
		setJMenuBar(new JMenuBar());
		getJMenuBar().add(Menu);

		text = new JTextArea(contents);
		text.setEditable(true);

		JScrollPane textSP = new JScrollPane(text);

		getContentPane().add(textSP);

		setSize(400, 300);
		UITools.center(null, this);
		show();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == revert) {
			text.setText(initialContents);
		}
		if (e.getSource() == save) {
			callback.callback(this, text.getText());
		}
		if (e.getSource() == exit) {
			int ret =
				JOptionPane.showConfirmDialog(
					null,
					"Save modifications?",
					"Question!",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (ret == JOptionPane.YES_OPTION) {
				callback.callback(this, text.getText());
			}
			if (ret == JOptionPane.NO_OPTION) {
				callback.callback(this, initialContents);
			}
			if (ret == JOptionPane.CANCEL_OPTION) {
				return;
			}
		}
	}

	public String getText() {
		return text.getText();
	}

	public String getInitialText() {
		return initialContents;
	}
}
