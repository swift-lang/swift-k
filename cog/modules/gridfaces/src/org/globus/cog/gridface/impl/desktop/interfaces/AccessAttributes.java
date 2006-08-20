//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.gridface.impl.desktop.interfaces;

import java.util.Hashtable;

public interface AccessAttributes {

	public static final String UNKNOWN_ATTRIB = "UNKNOWN ATTRIBUTE";
	
	public void clear();

	/**
	 * set attributes for the given command
	 */
	public void setAttribute(String name, Object value);

	/** Get the attribute with given name */
	public Object getAttribute(String name);

	/** Get all attributes in the form of a hash table */
	public Hashtable getAttributes();

	public void setAttributes(Hashtable attribs);

	public void removeAttribute(String name);
}
