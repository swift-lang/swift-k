
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 23, 2004
 */
package org.globus.cog.gui.grapheditor.canvas;


import java.util.EventObject;

import javax.swing.Icon;

public class StatusEvent extends EventObject {
	public static final int SET_DEFAULT_TEXT = 0;

	public static final int PUSH = 1;

	public static final int POP = 2;

	public static final int INITIALIZE_PROGRESS = 3;

	public static final int SET_PROGRESS = 4;

	public static final int STEP_PROGRESS = 5;

	public static final int REMOVE_PROGRESS = 6;

	public static final int ERROR = 7;

	public static final int WARNING = 8;
	
	public static final int INFO = 9;
	
	public static final int DEBUG = 10;
	
	public static final int OUT = 11;
	
	public static final int ERR = 12;

	private int type;

	private String msg;

	private Icon icon;

	private int value;

	private Exception details;

	public StatusEvent(Object source, int type, String msg, Icon icon, Exception details, int value) {
		super(source);
		this.type = type;
		this.msg = msg;
		this.details = details;
		this.value = value;
		this.icon = icon;
	}

	public StatusEvent(Object source, int type, String msg, Icon icon, int value) {
		this(source, type, msg, icon, null, value);
	}

	public Icon getIcon() {
		return icon;
	}

	public String getMsg() {
		return msg;
	}

	public int getType() {
		return type;
	}

	public int getValue() {
		return value;
	}

	public String toString() {
		return "Status event: type=" + type + ", msg=" + msg + ", icon=" + icon + ", value="
			+ value;
	}

	public Exception getDetails() {
		return this.details;
	}
}
