
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 12, 2004
 *
 */
package org.globus.cog.gui.grapheditor.util.swing;

import javax.swing.JToggleButton;

import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasAction;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionEvent;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionListener;

public class LinkedToggleButton extends JToggleButton implements CanvasActionListener {
	private CanvasAction action;
	
	public LinkedToggleButton(String name, CanvasAction action) {
		super(name);
		setSelected(action.isSelected());
		this.action = action;
		this.action.addCanvasActionListener(this);
	}

	public void canvasActionPerformed(CanvasActionEvent e) {
		if (e.getType() == CanvasActionEvent.ENABLED_STATE_CHANGED) {
			setEnabled(e.getCanvasAction().isEnabled());
		}
		else if (e.getType() == CanvasActionEvent.SELECTED_STATE_CHANGED) {
			setSelected(action.isSelected());
		}
	}
}
