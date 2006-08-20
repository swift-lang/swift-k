/*
 * 
 */
package org.globus.cog.gridshell.interfaces;

import java.beans.PropertyChangeListener;

/**
 * 
 */
public interface PropertyChangeNotifier {

	/**
	 * Add a property change listener
	 * @param pcListener
	 */
	public void addPropertyChangeListener(PropertyChangeListener pcListener);
	/**
	 * Add a property change listener for propertyName
	 * @param propertyName
	 * @param pcListener
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener pcListener);
	
	/**
	 * Remove a property change listener
	 * @param pcListener
	 */
	public void removePropertyChangeListener(PropertyChangeListener pcListener);
}
