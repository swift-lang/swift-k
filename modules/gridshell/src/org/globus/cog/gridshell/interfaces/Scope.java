/*
 * 
 */
package org.globus.cog.gridshell.interfaces;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import org.globus.cog.gridshell.model.ScopeException;

/**
 * Create a scope that variables can be stored in which may or may not have a super class
 * 
 */
public interface Scope extends Serializable, PropertyChangeNotifier {
	/**
	 * A class that designates the mode of this variable
	 * 
	 */
	public static class Mode {}
	
	/**
	 * Sets the variable to read only mode, it can't be written to
	 */
	public static final Mode READ_ONLY = new Mode();
	/**
	 * Sets the variable to read write mode, this is the default mode
	 */
	public static final Mode READ_WRITE = new Mode();
	
	/**
	 * Returns a collection of the variable names for this scope and any superclass
	 * @return
	 */
	Set getVariableNames();
	/**
	 * Gets the value associated with variable name for this or superclass
	 * @param name - the name of the variable to look for
	 * @return
	 */
	Object getValue(String name);
	/**
	 * Returns all the values (duplicates included)
	 * @return
	 */
	Collection getValues();
	/**
	 * Returns values (no duplicates)
	 * @return
	 */
	Set getUniqueValues();
	/**
	 * Returns a super class if one is defined, otherwise returns null
	 * @return
	 */
	Scope getSuper();
	/**
	 * If a variable "name" is defined in "this" sets the variable to value, next checks for the variable "name" up the superclass chain. If no such variable is found all the way up, creates a new variable this.name and sets to value 
	 * @param name - the name of the variable
	 * @param value - the value to set the variable to
	 * @throws ScopeException - thrown if failed to set the variable
	 */
	void setVariableTo(String name, Object value) throws ScopeException;
	/**
	 * Returns true of the variable exists either in "this" or up the super chain
	 * @param name
	 * @return
	 */	
	boolean variableExists(String name);	
	/**
	 * Returns the mode the variable is
	 * @param name
	 * @return
	 */
	Mode getMode(String name);
	/**
	 * Sets the mode of a variable
	 * @param name
	 * @param value
	 */
	void setMode(String name, Mode value);
	
}
