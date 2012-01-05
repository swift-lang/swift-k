
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.properties;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

/**
 * An interface that defines the methods used by objects that want to
 * use properties as in <code>Property</code>
 */
public interface PropertyHolder {

	public void addPropertyChangeListener(PropertyChangeListener l);

	public void removePropertyChangeListener(PropertyChangeListener l);

	public void firePropertyChange(PropertyChangeEvent e);

	public void firePropertyChange(String property, Object oldValue, Object newValue);

	public void firePropertyChange(String property);

	public void addProperty(Property property);

	public void removeProperty(Property property);

	public Property getProperty(String name);

	public Object getPropertyValue(String name);

	public void setPropertyValue(String name, Object value);

	public boolean hasProperty(String name);

	public Collection getProperties();
}
