/*
 * Created on May 25, 2005
 *
 */
package org.globus.cog.repository;

import java.sql.Connection;

/**
 * The RepositoryComponent Repository interface declares the methods necessary to eastablish connections, retrieve/update components in the repository 
 */
public interface Repository {
    /**
     * <code>setLocation</code> sets the provider information 
     * @param providerType 
     * @param hostName
     * @param port
     */
	public void setLocation(String providerType, String dbLocation);
	
	/**
	 * <code>connect</code> establishes a connection with the set repository. 
	 */
	public void connect();
	
	/**
	 * <code>disconnect</code> closes the connection with the respository.
	 */
	public void disconnect();
	
	/**
	 * Checks if the database connection is still valid
	 * @return Returns the status.
	 */
	public boolean isConnected();
	
	/**
	 * <code>setComponent</code> sets the component from the code Snippet  
	 * @param RepositoryComponent
	 * @param name
	 * @return Return true if value was set.
	 */
	public boolean setComponent(RepositoryComponent componentName, String name);
	
    /**
     * <code>getComponent</code> gets the component using the component name
     * @param componentName
     * @return
     */
	public RepositoryComponent getComponent(String componentName);
	
	/**
	 * <code>removeComponent</code> removes the component with <code>name</code> from the repository
	 * @param name
	 */
	public void removeComponent(String name);
	
	/**
	 * <code>search</code> returns an array of components that match the search query
	 * @param regex
	 * @return
	 */
	public String[] search(String attributeValuePair);
	
	/**
	 * <code>loadComponentFromFile</code> Loads all the components in the specified file into the repository
	 * @param fileName
	 */
    public void loadComponentsFromFile(String fileName);
    
    /**
     * <code>saveComponentsToFile</code> Saves all the components from the repository to a file with name <code>fileName</code> 
     * @param componentName
     * @param fileName
     * @param metadata
     */
    public void saveComponentsToFile(String componentName, String fileName, boolean metadata);

    
	public Connection getLocation();
	
}
