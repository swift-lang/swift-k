
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

import java.util.List;
import java.util.StringTokenizer;

import javax.swing.Icon;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.util.ConservativeArrayList;
import org.globus.cog.gui.grapheditor.util.EventDispatchHelper;

public class CanvasAction {
	private static Logger logger = Logger.getLogger(CanvasAction.class);
	
	public static final int ACTION_NORMAL = 0;
	public static final int ACTION_SWITCH = 1;
	public static final int ACTION_SELECTOR = 2;
	public static final int SEPARATOR = 4;
	public static final int SEPARATOR_BEFORE = 8;
	public static final int SEPARATOR_AFTER = 16;
	
	private static int ID = 0;
	
	private String name;
	private int type;
	private boolean selected;
	private boolean enabled;
	private List canvasActionListeners;
	private Icon icon;
	private int id = ID++;
	
	public CanvasAction(String name, Icon icon, int type) {
		this.name = name;
		this.type = type;
		this.icon = icon;
		this.enabled = true;
	}
	
	public CanvasAction(String name, int type) {
		this(name, null, type);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		if (selected != this.selected) {
			this.selected = selected;
			fireCanvasActionEvent(new CanvasActionEvent(this, CanvasActionEvent.SELECTED_STATE_CHANGED));
		}
	}
	
	public void perform() {
		logger.debug("Perform" + this);
		if (((type & 0x07) == ACTION_NORMAL) || ((type & 0x07) == ACTION_SELECTOR)) {
			fireCanvasActionEvent(new CanvasActionEvent(this, CanvasActionEvent.PERFORM));
		}
		else if ((type & 0x07) == ACTION_SWITCH) {
			setSelected(!isSelected());
		}
	}
	
	public void setSelectedQuiet(boolean selected) {
		this.selected = selected;
	}
	
	public String toString() {
		return "CanvasAction("+id+") name="+name+", type="+type+", selected="+selected;
	}
	
	public synchronized void addCanvasActionListener(CanvasActionListener l) {
		if (canvasActionListeners == null) {
			canvasActionListeners = new ConservativeArrayList();
		}
		if (!canvasActionListeners.contains(l)){
			canvasActionListeners.add(l);
		}
	}

	public synchronized void removeCanvasActionListener(CanvasActionListener listener) {
		if (canvasActionListeners != null) {
			canvasActionListeners.remove(listener);
		}
	}
	
	public synchronized void fireCanvasActionEvent(CanvasActionEvent mie) {
		EventDispatchHelper.fireCanvasActionEvent(canvasActionListeners, mie);
	}
	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}
	
	public boolean representsAction(String action) {
		StringTokenizer actionTokenizer = new StringTokenizer(action, ">");
		StringTokenizer nameTokenizer = new StringTokenizer(name, ">");
		if (actionTokenizer.countTokens() != nameTokenizer.countTokens()) {
			return false;
		}
		while (nameTokenizer.hasMoreTokens()) {
			String actionLevel = actionTokenizer.nextToken();
			String nameLevel = nameTokenizer.nextToken();
			if (nameLevel.indexOf('#') > 0) {
				nameLevel = nameLevel.substring(nameLevel.indexOf('#')+1);
			}
			if (!actionLevel.equals(nameLevel)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		if (enabled != this.enabled) {
			this.enabled = enabled;
			fireCanvasActionEvent(new CanvasActionEvent(this, CanvasActionEvent.ENABLED_STATE_CHANGED));
		}
	}

}