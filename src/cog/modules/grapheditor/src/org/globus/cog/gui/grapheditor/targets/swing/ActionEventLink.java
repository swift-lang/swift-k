
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 8, 2004
 *
 */
package org.globus.cog.gui.grapheditor.targets.swing;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.AbstractButton;

import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasAction;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionEvent;
import org.globus.cog.gui.grapheditor.util.EventConsumer;
import org.globus.cog.gui.grapheditor.util.EventDispatcher;

public class ActionEventLink implements ActionListener {
	private Map actionMap;

	private EventConsumer consumer;

	public ActionEventLink(Map actionMap, EventConsumer consumer) {
		this.actionMap = actionMap;
		this.consumer = consumer;
	}

	public void actionPerformed(ActionEvent e) {
		if (actionMap.containsKey(e.getSource())) {
			CanvasAction action = (CanvasAction) actionMap.get(e.getSource());
			action.setSelected(((AbstractButton) e.getSource()).isSelected());
			if (consumer == null) {
				return;
			}
			EventDispatcher.queue(consumer, new CanvasActionEvent(action,
				CanvasActionEvent.PERFORM));
		}
	}

}
