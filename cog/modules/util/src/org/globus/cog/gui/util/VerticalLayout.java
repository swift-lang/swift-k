// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 15, 2004
 */
package org.globus.cog.gui.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JSeparator;

public class VerticalLayout implements LayoutManager {
	private int border = 2;
	private boolean homogenous;

	public void removeLayoutComponent(Component comp) {
	}

	public void layoutContainer(Container parent) {
		int height = border;
		int width = parent.getWidth() - 2 * border;
		Component[] components = parent.getComponents();
		int prefsize = 2 * border;
		for (int i = 0; i < components.length; i++) {
			prefsize += components[i].getPreferredSize().height;
		}
		double exp = 0;
		if (homogenous) {
			exp = (double) parent.getHeight() / components.length;
		}
		for (int i = 0; i < components.length; i++) {
			if (!homogenous) {
				exp = (double) parent.getHeight() / prefsize
						* components[i].getPreferredSize().height;
			}
			if ((components[i] instanceof Container)
					&& ((((Container) components[i]).getLayout() instanceof VerticalLayout))
					|| (((Container) components[i]).getLayout() instanceof HorizontalLayout)) {
				components[i].setSize(width, (int) exp);
			}
			else {
				if (components[i] instanceof JSeparator) {
					components[i].setSize(width, 2);
				}
				else {
					components[i].setSize(components[i].getPreferredSize());
				}
			}
			double ha = components[i].getAlignmentX();
			double va = components[i].getAlignmentY();
			int left = (int) ((width - components[i].getWidth()) * ha) + border;
			components[i].setLocation(left, height + (int) ((exp - components[i].getHeight()) * va));
			height += exp;
		}
	}

	public void addLayoutComponent(String name, Component comp) {
	}

	public Dimension minimumLayoutSize(Container parent) {
		int maxw = 0;
		int height = 0;
		Component[] components = parent.getComponents();
		for (int i = 0; i < components.length; i++) {
			if (maxw < components[i].getMinimumSize().width) {
				maxw = components[i].getMinimumSize().width;
			}
			height = height + components[i].getMinimumSize().height;
		}
		return new Dimension(maxw + 2 * border, height + 2 * border);
	}

	public Dimension preferredLayoutSize(Container parent) {
		int maxw = 0;
		int height = 0;
		Component[] components = parent.getComponents();
		for (int i = 0; i < components.length; i++) {
			Dimension size = components[i].getPreferredSize();
			if (maxw < size.width) {
				maxw = size.width;
			}
			height = height + size.height;
		}
		return new Dimension(maxw + 2 * border, height + 2 * border);
	}

	public int getBorder() {
		return border;
	}

	public void setBorder(int border) {
		this.border = border;
	}

	public boolean isHomogenous() {
		return homogenous;
	}

	public void setHomogenous(boolean homogenous) {
		this.homogenous = homogenous;
	}
}