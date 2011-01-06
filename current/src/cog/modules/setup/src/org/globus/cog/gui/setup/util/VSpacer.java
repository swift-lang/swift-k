
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.util;

import java.awt.Dimension;

import javax.swing.JComponent;

import org.globus.cog.gui.util.SimpleGridLayout;

public class VSpacer extends JComponent {
	public VSpacer(int height) {
		super();
		setPreferredSize(new Dimension(SimpleGridLayout.Expand, height));
	}
}
