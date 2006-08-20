/*
 * 
 */
package org.globus.cog.gridshell.getopt.interfaces;

/**
 * An extended idea of a Variable.
 * 
 */
public interface Storable {
	
	/**
	 * Returns the description
	 * @return
	 */
	String getDescription();

	/**
	 * Returns the type
	 * @return
	 */
	Class getType();
	
	/**
	 * Returns the value based upon the validator associated with this type
	 * @return
	 */
	Object getValue();

	/**
	 * Tells if this is required or not
	 * @return
	 */
	boolean isRequired();

	/**
	 * Sees if this is set to non-null and not FALSE
	 * @return
	 */
	boolean isSet();

	/**
	 * Sets the value, if it is a String it reuses the validator associated with this type
	 * @param newValue
	 */
	void setValue(Object newValue);

}