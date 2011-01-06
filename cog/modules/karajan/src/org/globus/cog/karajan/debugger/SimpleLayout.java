//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 11, 2005
 */
package org.globus.cog.karajan.debugger;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class SimpleLayout implements LayoutManager {

	public void removeLayoutComponent(Component comp) {
	}

	public void layoutContainer(Container parent) {
		int y = 0;
		Component[] c = parent.getComponents();
		for (int i = 0; i < c.length; i++) {
			c[i].setLocation(0, y);
			c[i].setSize(c[i].getPreferredSize());
			y += c[i].getHeight();
		}
	}

	public void addLayoutComponent(String name, Component comp) {
	}

	public Dimension minimumLayoutSize(Container parent) {
		int maxmin = 0;
		int h = 0;
		Component[] c = parent.getComponents();
		for (int i = 0; i < c.length; i++) {
			Dimension d = c[i].getMinimumSize();
			int cmin = (int) d.width;
			if (maxmin < cmin) {
				maxmin = cmin;
			}
			h += d.height;
		}
		return new Dimension(maxmin, h);
	}

	public Dimension preferredLayoutSize(Container parent) {
		int maxpref = 0;
		int h = 0;
		Component[] c = parent.getComponents();
		for (int i = 0; i < c.length; i++) {
			Dimension d = c[i].getPreferredSize();
			int cmin = (int) d.width;
			if (maxpref < cmin) {
				maxpref = cmin;
			}
			h += d.height;
		}
		return new Dimension(maxpref, h);
	}

}
