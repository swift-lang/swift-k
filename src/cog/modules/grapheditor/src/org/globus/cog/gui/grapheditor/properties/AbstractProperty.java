
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.properties;


/**
 * Base class for properties. This whole concept of "soft" properties
 * allows for more flexibility than the java beans way.
 */
public abstract class AbstractProperty implements Property{    

    private int access;

    private String name;

    private Object owner;

    public AbstractProperty(Object owner, String name, int access) {
    	this.owner = owner;
    	this.name = name;
    	this.access = access;
    }

    public AbstractProperty(Object owner, String name) {
        this(owner, name, RW);
    }

    public int getAccess() {
        return access;
    }

    public void setAccess(int access) {
        this.access = access;
    }

    public boolean hasAccess(int access) {
        return (this.access & access) != 0;
    }

    public boolean isWritable() {
        return hasAccess(W);
    }

    public boolean isInteractive() {
        return hasAccess(X);
    }

    public boolean isHidden() {
        return hasAccess(HIDDEN);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return name;
    }

    public Object getOwner() {
        return owner;
    }

    public void setOwner(Object owner) {
        this.owner = owner;
    }

    public boolean equals(Object value) {
        if (value == this) {
            return true;
        }
        if (!(value instanceof Property)) {
            return false;
        }
        Property prop = (Property) value;
        if (!value.getClass().equals(getClass())) {
            return false;
        }
        return name.equals(prop.getName());
    }

    public int hashCode() {
        return getClass().hashCode() + name.hashCode();
    }
}
