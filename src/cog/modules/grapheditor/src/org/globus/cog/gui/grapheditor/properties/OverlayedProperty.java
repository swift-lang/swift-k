
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.properties;




/**
 * A "soft" property for which there is no direct support in the graph
 * component
 */
public class OverlayedProperty extends AbstractProperty {

    private Object value;

    public OverlayedProperty(PropertyHolder owner, String name, int access) {
        super(owner, name, access);
    }

    public OverlayedProperty(PropertyHolder owner, String name) {
        super(owner, name);
    }

    public void setValue(Object value) {
        Object oldValue = this.value;
        this.value = value;
        ((PropertyHolder) getOwner()).firePropertyChange(getName(), oldValue, value);
    }

    public Object getValue() {
        return value;
    }

    public Class getPropertyClass() {
        if (this.value != null) {
            return this.value.getClass();
        }
        else {
            return Object.class;
        }
    }
}
