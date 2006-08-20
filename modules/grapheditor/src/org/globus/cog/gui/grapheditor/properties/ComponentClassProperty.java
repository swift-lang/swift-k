
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


public class ComponentClassProperty extends AbstractClassProperty {
	private Class propertyClass;
	
	public ComponentClassProperty(Class cls, String name) {
		this(cls, name, Property.RW);
	}

	public ComponentClassProperty(Class cls, String name, int access) {
		super(cls, name, access);
		try {
			propertyClass = Introspector.getPropertyClass(cls, name);
		}
		catch (Exception e) {
			throw new RuntimeException("Invalid introspective property: " + name+" for node "+cls.getName());
		}
	}

	
	public Property getInstance(PropertyHolder owner) {
		Property inst = new ComponentProperty(owner, getName(), getAccess(), propertyClass);
		return inst;
	}
	
	public Class getPropertyClass() {
		return this.propertyClass;
	}
}
