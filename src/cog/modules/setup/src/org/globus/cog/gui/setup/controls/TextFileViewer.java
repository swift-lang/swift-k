// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.controls;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.globus.cog.gui.util.UITools;
import org.globus.cog.util.TextFileLoader;

/**
 * Displays a text file in a window
 */
public class TextFileViewer extends JDialog implements ActionListener {
	private JButton close;
	private boolean quit = false;
	private static Font monospace = new Font("Monospaced", Font.PLAIN, 11);

	public TextFileViewer(JFrame parent, String fileName, boolean resource) {
		super(parent, "File viewer");
		getContentPane().setLayout(new BorderLayout());
		setSize(600, 330);
		UITools.center(null, this);

		JTextArea textArea;

		try {
			TextFileLoader tfl = new TextFileLoader();
			if (resource) {
				textArea = new JTextArea(tfl.loadFromResource(fileName));
			}
			else {
				File f = new File(fileName);
				if (f.isDirectory()) {
					StringBuffer sb = new StringBuffer();
					String[] lst = f.list();
					for (int i = 0; i < lst.length; i++) {
						sb.append(lst[i]);
						sb.append('\n');
					}
					textArea = new JTextArea(sb.toString());
				}
				else {
					textArea = new JTextArea(tfl.loadFromFile(fileName));
				}
			}
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Cannot display file: " + e.getMessage(), "Error!",
					JOptionPane.ERROR_MESSAGE);
			quit = true;
			return;
		}

		if (monospace != null) {
			textArea.setFont(monospace);
		}
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);

		JScrollPane scroller = new JScrollPane(textArea);

		close = new JButton("Close");
		close.addActionListener(this);

		getContentPane().add(scroller, BorderLayout.CENTER);
		getContentPane().add(close, BorderLayout.SOUTH);
	}

	public TextFileViewer(JFrame parent, String fileName) {
		this(parent, fileName, false);
	}

	public void showDialog() {
		if (quit) {
			return;
		}
		show();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == close) {
			setVisible(false);
			dispose();
		}
	}

	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			setVisible(false);
			dispose();
		}
	}

}