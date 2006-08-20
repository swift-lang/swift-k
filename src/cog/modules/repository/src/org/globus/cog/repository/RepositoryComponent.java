/*
 * Created on May 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.globus.cog.repository;
import java.util.Enumeration;
import org.globus.cog.repository.impl.jdbc.MetaDataNotFoundException;

/**
 * The <code>RepositoryComponent</code> interface is used to represent the elements of data that are stored in the repository.
 */
public interface RepositoryComponent {
  

  /**
	 * The component is returned as an XML string
   * @param metadata the boolean used to specify if the metadata is to be added also
	 * @return Returns an XML String
 	 */
	public String toXML(boolean metadata);
	
  /**
   * Checks if the component exists in the repository
   * @param componentName
   * @return true if the component exists in the repository
   */  
  public boolean exists(String componentName);  
    
	/**
	 * <code>set</code> assigns a value to the component given the code
	 * @param code with metadata
	 * @throws MetaDataNotFoundException
	 */
	public void set(String fileName) throws MetaDataNotFoundException;

  /**
	 * <code>setName</code> sets the name for the component. This need not be sued if the name is available in the xml File. 
	 * @param compName
	 */
	public void setName(String compName);
    
	/**
	 * <code>setAttribute</code> sets an attribute from the available attributes or after creating a new attribute.
	 * @param attribute
	 * @param value
	 */
	public void setAttribute(String attribute, String value);
	
	/**
	 * <code>get</code> returns the component in its original form.
	 * @param name
	 * @return componentString
	 */
	public String get(String name);
	
	/**
	 * <code>getName</code> returns the name of the component.
	 * @return componentName
	 */
	public String getName();
	
	/**
	 * <code>getAttributes</code> Returns an enumeration of all the existing attributes for this component
	 * @return Returns an enumeration
	 */
	public Enumeration getAttributes();
	
	/**
	 * <code>getMetadata</code> returns a String with the components metadata information alone. 
	 * @return metadata
	 */
	public String getMetadata();

	
}
