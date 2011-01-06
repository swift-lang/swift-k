
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.panels;

import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *  Large big title dynamically changing for each component
 */
public class TitlePanel extends JPanel {
	private JLabel title;


	public TitlePanel() {
		super();

		setLayout(new FlowLayout(FlowLayout.LEFT));
		setBorder(BorderFactory.createEtchedBorder());

		title = new JLabel("Default title");
		title.setFont(new Font("SansSerif", 0, 18));
		add(title);
	}



	public void setTilte(String newTitle) {
		title.setText(newTitle);
	}



	public JLabel getLabel() {
		return title;
	}
}

