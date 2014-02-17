
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
/*
 * Created on Jun 24, 2003
 */
package org.globus.cog.gui.grapheditor.util.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import org.globus.cog.gui.grapheditor.util.ConservativeArrayList;

public class ComponentAction implements ActionListener{
	private List listeners;
	private Icon icon;
	private String name;
	
	public ComponentAction(String name, Icon icon){
		this.icon = icon;
		this.name = name;
	}
	
	public ComponentAction(String name){
		this(name, null);
	}
	
	public synchronized void addActionListener(ActionListener l){
		if (listeners == null){
		    listeners = new ConservativeArrayList(1);
		}
		if (!listeners.contains(l)){
			listeners.add(l);
		}
	}
	
	public synchronized void removeActionListener(ActionListener l){
		if (listeners != null){
		    listeners.remove(l);
		}
	}

	public synchronized void actionPerformed(ActionEvent e) {
		if (listeners == null){
		    return;
		}
		Iterator i = listeners.iterator();
		e.setSource(this);
		while(i.hasNext()){
			((ActionListener)i.next()).actionPerformed(e);
		}
	}
	
	public Component createComponent(){
		JMenuItem i = new JMenuItem(name);
		i.addActionListener(this);
		if (icon != null){
			i.setIcon(icon);
		}
		return i;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String string) {
		name = string;
	}
}
