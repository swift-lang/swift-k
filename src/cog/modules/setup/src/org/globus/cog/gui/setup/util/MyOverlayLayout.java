
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.setup.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class MyOverlayLayout implements LayoutManager {

	public void addLayoutComponent(String name, Component comp) {
	}

	public void removeLayoutComponent(Component comp) {
	}

	public Dimension preferredLayoutSize(Container parent) {
		return parent.getSize();
	}

	public Dimension minimumLayoutSize(Container parent) {
		return parent.getMinimumSize();
	}

	public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();
		Component[] components = parent.getComponents();
		for (int i = 0; i < components.length; i++) {
			components[i].setLocation(insets.left, insets.top);
			Dimension PSize = parent.getSize();
			int w = PSize.width - insets.left - insets.right;
			int h = PSize.height - insets.top - insets.bottom;
			components[i].setSize(w, h);
		}
	}
}
