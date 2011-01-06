
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


public abstract class AbstractClassProperty implements ClassProperty {
	private String name;

	private Class ownerClass;

	private int access;

	public AbstractClassProperty(Class ownerClass, String name, int access) {
		this.name = name;
		this.ownerClass = ownerClass;
		this.access = access;
	}

	public int getAccess() {
		return this.access;
	}

	public String getName() {
		return this.name;
	}

	public Class getOwnerClass() {
		return this.ownerClass;
	}
}
