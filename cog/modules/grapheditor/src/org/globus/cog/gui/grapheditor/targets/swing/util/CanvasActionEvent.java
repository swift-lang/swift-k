
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 21, 2004
 */
package org.globus.cog.gui.grapheditor.targets.swing.util;

import java.util.EventObject;


public class CanvasActionEvent extends EventObject{
	
	public final static int PERFORM = 0;
	public final static int SELECTED_STATE_CHANGED = 1;
	public final static int ENABLED_STATE_CHANGED = 2;
	
	private CanvasAction canvasAction;
	
	private int type;
	
	private boolean consumed;
	
	public CanvasActionEvent(CanvasAction menuItem, int type) {
		super(menuItem);
		this.canvasAction = menuItem;
		this.consumed = false;
		this.type = type;
	}
	public CanvasAction getCanvasAction() {
		return canvasAction;
	}

	public void setMenuItem(CanvasAction canvasAction) {
		this.canvasAction = canvasAction;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public String toString() {
		return "CanvasActionEvent type="+type+", menuItem=["+canvasAction+"]";
	}

	public boolean isConsumed() {
		return consumed;
	}

	public void setConsumed(boolean consumed) {
		this.consumed = consumed;
	}

}
