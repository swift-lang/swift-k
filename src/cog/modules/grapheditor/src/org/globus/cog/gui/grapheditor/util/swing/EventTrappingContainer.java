
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.util.swing;


import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;

public class EventTrappingContainer extends Container {
	protected Component comp;

	private boolean disabled;

	public EventTrappingContainer(Component comp) {
		setComponent(comp);
	}

	public void setComponent(Component comp) {
		this.comp = comp;
		if (comp != null) {
			comp.setVisible(true);
		}
	}

	public Component getComponent() {
		return comp;
	}

	public void enableMouseEvents() {
		disabled = false;
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
	}

	public void disableMouseEvents() {
		disabled = true;
		disableEvents(AWTEvent.MOUSE_EVENT_MASK);
	}

	public void disableEventsP(long mask) {
		disableEvents(mask);
	}

	public void enableEventsP(long mask) {
		enableEvents(mask);
	}

	public boolean contains(int x, int y) {
		if (disabled) {
			return false;
		}
		else {
			if ((x < getSize().width) && (x > 0) && (y < getSize().height) && (y > 0)) {
				return true;
			}
			return false;
		}
	}

	public void doLayout() {
		comp.doLayout();
	}

	/*
	 * public void processEvent(AWTEvent e){ super.processEvent(e);
	 */
}
