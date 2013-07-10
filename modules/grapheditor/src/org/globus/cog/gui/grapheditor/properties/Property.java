
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.properties;


public interface Property {
	//display-only property. no edit
	//typically used for something like a Swing component that needs to be
	// displayed but
	//not edited. On the other hand, inside Swing tables, it needs to be
	// editable to allow
	//mouse and keyboard interaction and live updates.
	public static final int X = 4;

	public static final int W = 2;

	public static final int R = 1;

	public static final int RW = R + W;

	//not saved normally
	public static final int HIDDEN = 16;

	//not saved under any circumstances
	public static final int NONPERSISTENT = 8;

	public static final int RWH = RW + HIDDEN;

	public int getAccess();

	public void setAccess(int access);

	public boolean hasAccess(int access);

	public boolean isWritable();

	public boolean isInteractive();

	public boolean isHidden();

	public String getName();

	public void setName(String name);

	public String getDisplayName();

	public Object getOwner();

	public void setOwner(Object owner);

	public void setValue(Object value);

	public Object getValue();

	public Class getPropertyClass();
}
