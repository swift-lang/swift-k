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

public class HorizontalLayout implements LayoutManager {
	private int border = 2;
	private boolean homogenous;

	public void removeLayoutComponent(Component comp) {
	}

	public void layoutContainer(Container parent) {
		int width = border;
		int height = parent.getHeight() - 2 * border;
		Component[] components = parent.getComponents();
		int prefsize = 2 * border;
		for (int i = 0; i < components.length; i++) {
			prefsize += components[i].getPreferredSize().width;
		}
		double exp = 0;
		if (homogenous) {
			exp = (double) parent.getWidth() / components.length;
		}
		for (int i = 0; i < components.length; i++) {
			if (!homogenous) {
				exp = (double) parent.getWidth() / prefsize
						* components[i].getPreferredSize().width;
			}
			if ((components[i] instanceof Container)
					&& ((((Container) components[i]).getLayout() instanceof VerticalLayout))
					|| (((Container) components[i]).getLayout() instanceof HorizontalLayout)) {
				components[i].setSize((int) exp, height);
			}
			else {
				if (components[i] instanceof JSeparator) {
					components[i].setSize(2, height);
				}
				else {
					components[i].setSize(components[i].getPreferredSize());
				}
			}
			double ha = components[i].getAlignmentX();
			double va = components[i].getAlignmentY();
			int top = (int) ((height - components[i].getHeight()) * va) + border;
			components[i].setLocation(width + (int) ((exp - components[i].getWidth()) * ha), top);
			width += exp;
		}
	}

	public void addLayoutComponent(String name, Component comp) {
	}

	public Dimension minimumLayoutSize(Container parent) {
		int maxh = 0;
		int width = 0;
		Component[] components = parent.getComponents();
		for (int i = 0; i < components.length; i++) {
			if (maxh < components[i].getMinimumSize().height) {
				maxh = components[i].getMinimumSize().height;
			}
			width = width + components[i].getMinimumSize().width;
		}
		return new Dimension(width + 2 * border, maxh + 2 * border);
	}

	public Dimension preferredLayoutSize(Container parent) {
		int maxh = 0;
		int width = 0;
		Component[] components = parent.getComponents();
		for (int i = 0; i < components.length; i++) {
			Dimension size = components[i].getPreferredSize();
			if (maxh < size.height) {
				maxh = size.height;
			}
			width = width + size.width;
		}
		return new Dimension(width + 2 * border, maxh + 2 * border);
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