
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
public class DelegatedProperty extends AbstractProperty {

    private String delegatedName;
    private String displayName;
    private Class propertyClass;
    
    protected DelegatedProperty(PropertyHolder owner, String name, String displayName, String delegatedName, int access, Class propertyClass) {
        super(owner, name, access);
        this.delegatedName = delegatedName;
        this.displayName = displayName;
        this.propertyClass = propertyClass;
    }

    public DelegatedProperty(PropertyHolder owner, String name, String displayName, String delegatedName, int access) {
        super(owner, name, access);
        this.delegatedName = delegatedName;
        this.displayName = displayName;
        if (owner != null) {
            try {
                propertyClass = Introspector.getPropertyClass(owner, delegatedName);
            }
            catch (Exception e) {
                throw new RuntimeException("Invalid introspective property");
            }
        }
        else {
            propertyClass = Object.class;
        }
    }

    public DelegatedProperty(PropertyHolder owner, String name, String delegatedName, int access) {
        this(owner, name, name, delegatedName, access);
    }

    public DelegatedProperty(PropertyHolder owner, String name, String displayName, String delegatedName) {
        this(owner, name, displayName, delegatedName, RW);
    }

    public DelegatedProperty(PropertyHolder owner, String name, String delegatedName) {
        this(owner, name, delegatedName, RW);
    }

    public String getDelegatedName() {
        return delegatedName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setValue(Object value) {
        Introspector.setProperty(getOwner(), delegatedName, value, propertyClass);
    }

    public Object getValue() {
        return Introspector.getProperty(getOwner(), delegatedName);
    }

    public Class getPropertyClass() {
        return propertyClass;
    }
}
