
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.panels;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.globus.cog.gui.setup.controls.TextFileViewer;

/**
 *  Implements the panel containing the about button and its logic
 */
public class AboutPanel extends JPanel implements ActionListener {
	private JButton about;

	/**
	 *  Constructor for the AboutPanel object
	 */
	public AboutPanel() {
		super();
		setLayout(new FlowLayout());
		setBorder(BorderFactory.createEtchedBorder());

		about = new JButton("About");
		about.addActionListener(this);

		add(about);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == about) {
			TextFileViewer ATV = new TextFileViewer(null, "text/setup/about.txt", true);

			ATV.showDialog();
		}
	}
}
