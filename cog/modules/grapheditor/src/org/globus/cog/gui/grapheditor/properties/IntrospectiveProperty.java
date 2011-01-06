
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.properties;



/**
 * A property class which uses getters an setters to get/set the value
 * of a property
 */
public class IntrospectiveProperty extends AbstractProperty {
	private Class propertyClass;
	
	protected IntrospectiveProperty(PropertyHolder owner, String name, int access, Class propertyClass) {
		super(owner, name, access);
		this.propertyClass = propertyClass;
	}

    public IntrospectiveProperty(Object owner, String name, int access) {
        super(owner, name, access);
        if (owner != null) {
            try {
                propertyClass = Introspector.getPropertyClass(owner, name);
            }
            catch (Exception e) {
                throw new RuntimeException("Invalid introspective property: " + name+" for node "+owner.getClass());
            }
        }
    }

    public IntrospectiveProperty(Object owner, String name) {
        this(owner, name, RW);
    }

    public void setValue(Object value) {
        Introspector.setProperty(getOwner(), getName(), value, propertyClass);
    }

    public Object getValue() {
        return Introspector.getProperty(getOwner(), getName());
    }

    public Class getPropertyClass() {
	    return propertyClass;
    }
}
