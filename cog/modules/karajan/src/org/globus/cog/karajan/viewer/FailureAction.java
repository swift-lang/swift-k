
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 10, 2003
 */
package org.globus.cog.karajan.viewer;


import java.awt.Component;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JRadioButton;

import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.events.NotificationEvent;

public abstract class FailureAction {
	private JRadioButton comp;

	public FailureAction() {
		initializeComponent();
	}

	public FailureAction newInstance() {
		try {
			FailureAction newInstance = (FailureAction) this.getClass()
				.newInstance();
			return newInstance;
		}
		catch (InstantiationException e) {
			return null;
		}
		catch (IllegalAccessException e) {
			return null;
		}
	}

	public abstract void handleFailure(EventListener element,
		NotificationEvent event);

	public boolean isComplete() {
		return true;
	}

	public String getName() {
		return "None";
	}

	public String getDescription() {
		return "None";
	}

	public Icon getIcon() {
		return null;
	}

	public Component getComponent(ButtonGroup group) {
		if (group != null) {
			group.add(comp);
		}
		return comp;
	}

	protected void initializeComponent() {
		comp = new JRadioButton(getName());
		comp.setToolTipText(getDescription());
	}

	public boolean isSelected() {
		return comp.isSelected();
	}
}
