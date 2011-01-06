
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 24, 2004
 */
package org.globus.cog.gui.grapheditor.util.swing;

import javax.swing.JButton;

import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasAction;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionEvent;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionListener;

public class LinkedButton extends JButton implements CanvasActionListener{
	private CanvasAction menuItem;
	
	public LinkedButton(String name, CanvasAction menuItem) {
		super(name);
		setSelected(menuItem.isSelected());
		this.menuItem = menuItem;
		this.menuItem.addCanvasActionListener(this);
	}

	public void canvasActionPerformed(CanvasActionEvent e) {
		if (e.getType() == CanvasActionEvent.ENABLED_STATE_CHANGED) {
			setEnabled(e.getCanvasAction().isEnabled());
		}
	}
}
