// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 23, 2005
 */
package org.globus.cog.gui.util;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class RegistrationFrame extends JFrame implements ActionListener {
	private RegistrationPanel panel;
	private JButton submit, cancel;
	private boolean done;

	public RegistrationFrame() {
		JPanel main = new JPanel();
		main.setLayout(new BorderLayout());
		main.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		JLabel label = new JLabel("Java CoG Kit Registration");
		label.setFont(Font.decode("Arial-bold-18"));
		label.setBorder(BorderFactory.createEmptyBorder(5, 10, 20, 10));
		
		main.add(label, BorderLayout.NORTH);

		panel = new RegistrationPanel();
		panel.getReregister().addActionListener(this);
		main.add(panel, BorderLayout.CENTER);
		setTitle("Java CoG Kit Registration Form");

		JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout());

		submit = new JButton("Submit");
		submit.addActionListener(this);
		buttons.add(submit);

		submit.setEnabled(panel.getReregister().isSelected());

		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		buttons.add(cancel);

		main.add(buttons, BorderLayout.SOUTH);
		
		getContentPane().add(main);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == submit) {
			try {
				panel.submit(false);
				JOptionPane.showMessageDialog(this, "Thank you for registering the Java CoG Kit",
						"Registration successful", JOptionPane.INFORMATION_MESSAGE);
				done();
			}
			catch (IOException e1) {
				JOptionPane.showMessageDialog(this, "Could not submit registration information: "
						+ e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e.getSource() == cancel) {
			done();
		}
		else {
			//must be the don't send switch
			submit.setEnabled(panel.getReregister().isSelected());
		}
	}

	private void done() {
		done = true;
		synchronized (this) {
			notify();
		}
	}

	public void run() {
		setSize(500, 380);
		UITools.center(null, this);
		setVisible(true);
		try {
			synchronized (this) {
				while (!done) {
					wait();
				}
			}
		}
		catch (InterruptedException e) {
			JOptionPane.showMessageDialog(this, "The main thread was interrupted", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		setVisible(false);
		dispose();
	}

	public static void main(String[] args) {
		RegistrationFrame frame = new RegistrationFrame();
		frame.run();
		System.exit(0);
	}
}
