
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 7, 2004
 *
 */
package org.globus.cog.gui.grapheditor.properties;

public interface ClassProperty {
	public Property getInstance(PropertyHolder owner);
	
	public Class getOwnerClass();
	
	public String getName();
	
	public int getAccess();
}
