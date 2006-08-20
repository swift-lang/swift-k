/*
 * Created on May 27, 2005
 *
 */
package org.globus.cog.repository.impl.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.globus.cog.repository.RepositoryAttributes;
import org.globus.cog.repository.RepositoryFactory;

/**
 * <code>Derby Attributes</code> is used to update the attribute values for a repository. By adding or updating the attributes here, 
 * the changes will be made to the corresponding component tables. In future, this would help in maintaining multiple repositories
 * each referred to by different IDs. The support for this feature is not currently available.  This is an implementation of the 
 * <code>RepositoryAttributes</code>
 */
public class DerbyRepositoryAttributes implements RepositoryAttributes {
	private Connection conn;
	private PreparedStatement pStmt;
	private HashMap attributeMap; //using hashmap as I dont think synchronization is necessary here and performance wise a hashmapis better
	private String name;
  private RepositoryFactory factory;

  private static Logger logger = Logger.getLogger(DerbyRepositoryAttributes.class);
	
  public DerbyRepositoryAttributes(){
		attributeMap = new HashMap();
	}
  
	public DerbyRepositoryAttributes(String name, Connection conn){
		attributeMap = new HashMap();
		this.conn = conn;
		this.name = name;
	}
	
  public void setConn(Connection conn) {
    this.conn = conn;
  }
  
  public Connection getConn() {
    return this.conn;
  }
  
	/* (non-Javadoc)
	 * @see org.globus.cog.abstraction.repository.ComponentAttributes#get(java.lang.String)
	 */
	public String get(String name) {
		String attType = new String();
		try {
			logger.debug("Selecting -" + name + "-");
			pStmt = conn.prepareStatement(
					"SELECT * FROM component_attributes WHERE attr_id = ?");
			pStmt.setString(1, name);
			ResultSet rs = pStmt.executeQuery();
			if(rs.next()){
			attType = rs.getString("attr_type");
			}
			logger.debug("Attribute Type - " + attType + "for " + name);
		} catch (SQLException e) {
      logger.debug("Was unable to get attribute " + name + "'s information");
			e.printStackTrace();
		}
			return attType;
	}
    
	/**
	 * Adds a new attribute to the table and updates the repository with new columns
	 * @param attributeName the string to be set as the attribute name
	 * @param description the string describing the data to be stored in the attribute
	 * @param attributeType the string that is used as the datatype for the attribute within the repository
	 * @see org.globus.cog.repository.RepositoryAttributes#set(java.lang.String, java.lang.String)
	 */
	public void set(String attributeName, String description, String attributeType) {
		try {
			
			//TODO: Check below if attribute already exists and then make an update if so.
			//Delete the row corresponding to the attribute Name from component_attributes
		pStmt = conn.prepareStatement(
			"INSERT INTO component_attributes VALUES( ?, ?, ?)");
		pStmt.setString(1, attributeName);
		pStmt.setString(2, description);
		pStmt.setString(3, attributeType);
		pStmt.executeUpdate();
    logger.debug("Inserted " + attributeName + "into table");
    
		pStmt = conn.prepareStatement(
				"ALTER TABLE component_metadata ADD COLUMN ? ?");
		pStmt.setString(1, attributeName);
		pStmt.setString(2, attributeType);
		pStmt.executeUpdate();
    logger.debug("Altered the metadata table adding column " + attributeName );
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Removes the attribute from the list of available attributes. Removing an attribute involves 
	 * also dropping that column from the other tables. Derby does not support this currently. 
	 * This feature will be updated once Derby makes dropping of columns possible. 
	 * @param the string that represents the name of the attribute  
	 * @see org.globus.cog.repository.RepositoryAttributes#remove(java.lang.String)
	 */
	public void remove(String attributeName) {
		try {
			//Delete the row corresponding to the attribute Name from component_attributes
			pStmt = conn.prepareStatement(
				"DELETE * FROM component_attributes WHERE attr_id = ?");
			pStmt.setString(1, attributeName);
			pStmt.executeUpdate();
	        //Delete the column from component_metadata that corresponds to the attribute name 
			//You will get an error if an attempt is made to delete the primary key. 
			//TODO: This feature - Dropping columns is not yet available in cloudscape. Once this is done the code will be updated.
			/*pStmt = conn.prepareStatement(
			"ALTER TABLE component_metadata DROP COLUMN ?");
			pStmt.setString(1, attributeName);
			pStmt.executeUpdate();*/
      logger.debug("Removed row with attr_id " + attributeName);
		} catch (SQLException e) {
      logger.debug("Unable to remove row with attr_id "+ attributeName  );
			e.printStackTrace();
		}	

	}

	/**
	 * Lists the attributes that are available for the components within this repository
	 * @return an enumeration of the attributes available. 
	 * @see org.globus.cog.repository.RepositoryAttributes#listAttributes()
	 */
	public Enumeration listAttributes() {
		Vector attList = new Vector();
		try {
			pStmt = conn.prepareStatement("SELECT attr_id FROM component_attributes");
		ResultSet rs = pStmt.executeQuery();
			while(rs.next()){
				attList.add(rs.getString("attr_id"));
				logger.debug("Attr _ Id " + rs.getString("attr_id"));
			}
			
		} catch (SQLException e) {
      logger.debug("Unable to retrieve attributeId values from component_attributes");
			e.printStackTrace();
		}
		
		return attList.elements();
	}

}
