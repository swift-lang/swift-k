
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------
    
package org.globus.cog.gui.setup.controls;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 *  Displays a text file in a window
 */
public class PasswordInput {
	private JOptionPane OP;
	private JPasswordField pass;
	private JDialog dialog;

	public PasswordInput(String title, String message) {
		OP = new JOptionPane();

		JPanel panel = new JPanel(new BorderLayout());

		panel.add(new JLabel(message), BorderLayout.NORTH);
		pass = new JPasswordField();
		pass.setPreferredSize(new Dimension(100, 20));
		panel.add(pass, BorderLayout.CENTER);
		OP.setMessage(panel);
		OP.setOptionType(JOptionPane.OK_CANCEL_OPTION);
		dialog = OP.createDialog(null, title);
	}

	public String getPassword() {
		dialog.show();
		return new String(pass.getPassword());
	}

	public boolean wasOk() {
		return (((Integer) OP.getValue()).intValue() == JOptionPane.OK_OPTION);
	}
}
