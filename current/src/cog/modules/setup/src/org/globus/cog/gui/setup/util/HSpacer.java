
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.setup.util;

import java.awt.Dimension;

import javax.swing.JComponent;

public class HSpacer extends JComponent {
	public HSpacer(int width) {
		super();
		setPreferredSize(new Dimension(width, 1));
	}
}
