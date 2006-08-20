/*
 * Created on May 25, 2005
 *
 */
package org.globus.cog.repository;

import java.util.Enumeration;

/**
 * Interface to set and get new Attribute types for a repository.
 */
public interface RepositoryAttributes {

	/**
	 * <code>get</code> is used to get the attribute details
	 * @param name
	 * @return Returns the details as a string
	 */
	public String get(String name);
	
	/**
	 * <code>set</code> used to set a type for the attribute with <code>attributeName</code>
	 * @param attributeName
	 * @param attributeType
	 */
	public void set(String attributeName, String description, String attributeType);
	
	/**
	 * <code>remove</code> is to remove a attribute type with <code>name</code> from the repository
	 * @param attributeName
	 */
	public void remove(String attributeName);
	
	/**
	 * <code>listAttributes</code> returns an enumeration of all the allowed attributes in the repository.
	 * @return Returns an enumeration
	 */
	public Enumeration listAttributes();
	
}
