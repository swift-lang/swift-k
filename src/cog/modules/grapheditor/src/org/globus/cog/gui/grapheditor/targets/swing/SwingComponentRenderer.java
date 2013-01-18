
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 23, 2004
 */
package org.globus.cog.gui.grapheditor.targets.swing;

import java.awt.Component;
import java.util.List;

import org.globus.cog.gui.grapheditor.ComponentRenderer;
import org.globus.cog.gui.grapheditor.util.swing.ComponentAction;

public interface SwingComponentRenderer extends ComponentRenderer{
	public abstract Component getVisualComponent();
	public abstract void setVisualComponent(Component component);
	/**
     * Adds an action to this component. An action represents something that can be
     * performed on this component. In the swing target actions are rendered in a 
     * pop-up menu.
     */
	public void addAction(ComponentAction a);
	
	/**
	 * Returns a list of actions that were added to this component.
	 */
	public List getActions();
	
	/**
	 * Returns an action with the name specified by the name parameter
	 */
	public ComponentAction getAction(String name);
	
	/**
	 * Removes an action
	 */
	public void removeAction(ComponentAction action);
}