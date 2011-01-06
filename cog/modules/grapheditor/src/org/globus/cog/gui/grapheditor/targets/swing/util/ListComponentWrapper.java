
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.targets.swing.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;

public class ListComponentWrapper extends Container{
	private Component component;
	public void paint(Graphics g){
		component.setSize(getSize());
		super.paint(g);
		g.setColor(Color.black);
		g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
	}
	
	public void setComponent(Component component) {
		this.component = component;
		removeAll();
		component.setLocation(0,0);
		component.setSize(component.getPreferredSize());
		add(component);
	}
}

