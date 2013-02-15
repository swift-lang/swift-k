
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.panels;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.globus.cog.util.ImageLoader;

/**
 *  Contains the logo! This one of the fundamental elements in the wizard
 */
public class LogoPanel extends JPanel {

	public LogoPanel() {
		super();
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEtchedBorder());

		JLabel globusLabel;
		ImageLoader il = new ImageLoader();
		ImageIcon globusIcon = il.loadImage("images/logos/globus.png");

		if (globusIcon != null) {
			globusLabel = new JLabel(globusIcon);
		}
		else {
			globusLabel = new JLabel("Globus Logo");
		}
		add(globusLabel, BorderLayout.CENTER);
	}
}
