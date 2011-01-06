
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.properties;


/**
 * This type of property can be used when the name of the property is
 * different than the name used for the getters and setters.
 */
public class DelegatedClassProperty extends AbstractClassProperty {

	private String delegatedName;

	private String displayName;

	private Class propertyClass;

	public DelegatedClassProperty(Class ownerClass, String name, String displayName,
		String delegatedName, int access) {
		super(ownerClass, name, access);
		this.delegatedName = delegatedName;
		this.displayName = displayName;
		try {
			propertyClass = Introspector.getPropertyClass(ownerClass, delegatedName);
		}
		catch (Exception e) {
			throw new RuntimeException("Invalid delegated class property " + name + " for "
				+ ownerClass.getName());
		}
	}

	public DelegatedClassProperty(Class ownerClass, String name, String delegatedName, int access) {
		this(ownerClass, name, name, delegatedName, access);
	}

	public DelegatedClassProperty(Class ownerClass, String name, String displayName,
		String delegatedName) {
		this(ownerClass, name, displayName, delegatedName, Property.RW);
	}

	public DelegatedClassProperty(Class ownerClass, String name, String delegatedName) {
		this(ownerClass, name, delegatedName, Property.RW);
	}

	public String getDelegatedName() {
		return delegatedName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Property getInstance(PropertyHolder owner) {
		DelegatedProperty inst = new DelegatedProperty(owner, getName(), getDisplayName(),
			getDelegatedName(), getAccess(), propertyClass);
		return inst;
	}
}
