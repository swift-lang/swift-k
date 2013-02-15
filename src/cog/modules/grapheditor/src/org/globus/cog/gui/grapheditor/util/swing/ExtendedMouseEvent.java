/*
 * Created on Jun 15, 2004
 */
package org.globus.cog.gui.grapheditor.util.swing;

import java.awt.Component;
import java.awt.event.MouseEvent;


public class ExtendedMouseEvent extends MouseEvent {

	private int invokerX, invokerY;
	private Component popupInvoker;

	public ExtendedMouseEvent(Component source, int id, long when, int modifiers, int x,
		int y, int clickCount, boolean popupTrigger) {
		super(source, id, when, modifiers, x, y, clickCount, popupTrigger);
		invokerX = -1;
		invokerY = -1;
	}

	public ExtendedMouseEvent(Component source, int id, long when, int modifiers, int x,
		int y, int clickCount, boolean popupTrigger, int button) {
		super(source, id, when, modifiers, x, y, clickCount, popupTrigger, button);
		invokerX = -1;
		invokerY = -1;
	}


	public int getInvokerX() {
		return invokerX;
	}

	public void setInvokerX(int screenX) {
		this.invokerX = screenX;
	}

	public int getInvokerY() {
		return invokerY;
	}

	public void setInvokerY(int screenY) {
		this.invokerY = screenY;
	}

	public Component getPopupInvoker() {
		return popupInvoker;
	}

	public void setPopupInvoker(Component popupInvoker) {
		this.popupInvoker = popupInvoker;
	}
}