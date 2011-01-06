
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.util.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;


public class TransparentContainer extends EventTrappingContainer implements ComponentListener{
	
	public TransparentContainer(Component comp){
		super(comp);
	}
	
	public void setComponent(Component comp){
		if (getComponent() != null){
			getComponent().removeComponentListener(this);
		}
		removeAll();
		super.setComponent(comp);
		add(comp);
		comp.setLocation(0, 0);
		comp.addComponentListener(this);
		setSize(comp.getPreferredSize());
	}
	
	public Dimension getPreferredSize(){
		return getComponent().getPreferredSize();
	}
	
	public void setBounds(Rectangle rect){
		super.setBounds(rect);
		getComponent().setSize(rect.width, rect.height);
		getComponent().invalidate();
		getComponent().validate();
	}
	
	public void setSize(int w, int h){
		Point p = getLocation();
		setBounds(new Rectangle(p.x, p.y, w, h));
	}
	
	public void setSize(Dimension d){
		setSize(d.width, d.height);
	}
		
	public void componentResized(ComponentEvent e) {
		if (!getSize().equals(getComponent().getSize())){
			super.setSize(getComponent().getSize());
		}
	}
	
	public void componentMoved(ComponentEvent e) {
	}
	
	public void componentShown(ComponentEvent e) {
	}
	
	public void componentHidden(ComponentEvent e) {
	}
}

