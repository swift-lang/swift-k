
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.properties;



/**
 * This type of property is specific to graph components.
 * It is introspective and it fires a property change event.
 */
public class ComponentProperty extends IntrospectiveProperty {
	
	protected ComponentProperty(PropertyHolder owner, String name, int access, Class propertyClass) {
		super(owner, name, access, propertyClass);
	}

    public ComponentProperty(PropertyHolder owner, String name, int access) {
        super(owner, name, access);
    }

    public ComponentProperty(Object owner, String name) {
        super(owner, name);
    }

    public void setValue(Object value) {
        Object oldValue = Introspector.getProperty(getOwner(), getName());
        super.setValue(value); 
        ((PropertyHolder) getOwner()).firePropertyChange(getName(), oldValue, value);
    }
}
