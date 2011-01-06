
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor.targets.swing.views;

import java.awt.Component;

import org.globus.cog.gui.grapheditor.canvas.views.AbstractView;
import org.globus.cog.gui.grapheditor.targets.swing.SwingCanvasRenderer;

public abstract class SwingView extends AbstractView {
	private Component component;
	
	public Component getComponent() {
		return component;
	}
	
	public void setComponent(Component component) {
		this.component = component;
	}
	
	public SwingCanvasRenderer getSwingRenderer() {
		return (SwingCanvasRenderer) getRenderer();
	}
	
	public void repaint() {
		if (getComponent() != null) {
			if (getComponent() != null) {
				getComponent().repaint();
			}
		}
	}
	
	public void activate() {
		super.activate();
		enable();
	}
	
	public void clean() {
		disable();
		super.clean();
	}
	
	public void disable() {
		
	}
	
	public void enable() {
		
	}
}
