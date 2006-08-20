/*
 * Created on May 27, 2005
 *
 */
package org.globus.cog.repository.impl.jdbc;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.globus.cog.repository.XMLUtil.ComponentParser;
import org.globus.cog.repository.RepositoryComponent;
import org.globus.cog.repository.RepositoryAttributes;
import org.globus.cog.repository.RepositoryFactory;
import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

/**
 * <code>Derby RepositoryComponent</code> implements the <code>RepositoryComponent</code>
 * interface.
 * This class is used to set the metadata, code and the attributes for
 * a component before the <code>Derby Repository</code> stores it in
 * the repository. The information may be retrieved from the database
 * in the form of an XML document.
 */


public class DerbyRepositoryComponent implements RepositoryComponent {

	private RepositoryAttributes compAttributes;
	public String name;
  public String code;
	private Hashtable attributeTable;
  public Connection conn;
  private PreparedStatement pStmt;
  private RepositoryFactory factory;

  private static Logger logger = Logger.getLogger(DerbyRepositoryComponent.class);
	
	public DerbyRepositoryComponent(String name, Connection conn){
		attributeTable = new Hashtable();
    this.conn = conn;
		this.name = name;
    compAttributes = RepositoryFactory.newRepositoryAttributes("derby");	
	}
	
	public DerbyRepositoryComponent(){
		attributeTable = new Hashtable();
    compAttributes = RepositoryFactory.newRepositoryAttributes("derby");
	}
	
	/** 
   * Converts the component to an XML file. 
   * @param metadata the boolean used to specify if the metadata 
   * is to be included in the generated file
   * @return the XML file in the form of String
	 * @see org.globus.cog.abstraction.repository.Component#toXML()
	 **/
	public String toXML(boolean metadata) {
    String xmlDoc = new String();
    //  Creating the XML Elements to add to XML file String
    Element componentElement = new Element("component",
        "http://cogkit.org/cog-workflow/karajan/");
    Namespace xsi = Namespace.getNamespace("xsi", 
        "http://www.w3.org/2001/XMLSchema-instance");
    //componentElement.addNamespaceDeclaration(xsi);
    componentElement.setAttribute(
        new Attribute("schemaLocation",
        "http://cogkit.org/cog-workflow/karajan/ component.xsd",
        xsi));
    Document xmlDocument = new Document(componentElement);
    
    if(metadata){
        //Retrieving metadata from the repository
        Enumeration attrNamesArray = null;
        // obtain the list of attributeNames for the current repository
        ((DerbyRepositoryAttributes) compAttributes).setConn(conn);
        attrNamesArray = compAttributes.listAttributes();
        // obtain a list of the attributeValues for the current repository
        Enumeration attrList =  getAttributes();
      
      Element metadataElement = new Element("metadata");
      while(attrNamesArray.hasMoreElements()){
      String elementName = attrNamesArray.nextElement().toString();   
      String elementContent = attrList.nextElement().toString();
      metadataElement.addContent(new Element(elementName).addContent(elementContent));
      }   
      componentElement.addContent(metadataElement);
    }
    
    Element sourceElement = new Element("source");
    //Retrieving the source from the repository
    String source = get(this.name);
    CDATA sourceCDATA = new CDATA(source);
    sourceElement.addContent(sourceCDATA);
    componentElement.addContent(sourceElement);
    
    XMLOutputter outputter = new XMLOutputter();
    xmlDoc = outputter.outputString(xmlDocument);
		return xmlDoc;
	}
  
  /**
   * Writes the component sourcecode alone to a temporaryfile
   * @return
   */
  
  public String toFile(String filename) {
    String fileData = this.toString();
    String status = "File not saved";
    try {
      BufferedWriter textWriter = new BufferedWriter(new FileWriter(filename));
      textWriter.write(fileData);
      textWriter.close();
      status = "File saved";
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }   
    return status; 
  }
  
  /**
   * Converts the component source into a string snippet. 
   * Requires that the component name be set prior to this method call.
   * 
   * @return the string value of the component source 
   */
  public String toString(){
    String componentSource = get(this.name);
    return componentSource;
  }
  
  /**
   * Checks if the specified component exists in the repository
   * @param componentName the string that represents the name of the component
   * @return true of the file exists
   */  
  public boolean exists(String componentName) {
   try {
         pStmt = conn.prepareStatement(
                 "SELECT comp_id from component_metadata");
         ResultSet rs = pStmt.executeQuery();
         while(rs.next()){
         if(rs.getString("comp_id").trim().equalsIgnoreCase(componentName)){
           return true;
          }
         }
   }
   catch (SQLException e) {
         logger.debug("Was unable to retrieve component " + componentName + " from component_metadata ");
         e.printStackTrace();
       }    
   return false;
  }
    
    
    /**
     * Sets the values for the component object's <code>code</code>. 
     * Updates to the database(component_code) are made only through the component repository's setComponent function.
     * 
     * @param fileName the string that points to the file to be stored in the repository   
     * @throws MetaDataNotFoundException
     * @see org.globus.cog.repository.RepositoryComponent#set(java.lang.String, java.lang.String)
     */
	public void set(String fileName, Connection conn) throws MetaDataNotFoundException {
    //TODO: if component name already exists set the name as deprecated
    
    logger.debug("Parsing the file " + fileName);
		ComponentParser parser = new ComponentParser(fileName);
		parser.validate();
    logger.debug("Validated the xml file");
		if(this.name == null) this.name = parser.getComponentName();
    conn = this.conn;
		this.code = parser.parseCode();
		// setting the code happens only in DerbyRepository.setComponent()
		//TODO: check if that value already exists and do an update if value exists.
		// not insert.
		/*try {
			pStmt =  conn.prepareStatement(
					"INSERT INTO component_code VALUES( ?, ? )");
			pStmt.setString(1, name);
			pStmt.setString(2, code);
			pStmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}*/
		Hashtable metadataTable = parser.parseMetaData();
		//setting the metadata
		// if metadata doesnot exist then send to error and ask user to enter 
		// metadata using wither setattribute or directly in the file.  
		if(metadataTable == null){
			throw new MetaDataNotFoundException("Metadata doesn't exist. Set using setAttribute or add to the xmlFile");
		}
		// if metadata exists then set to the attribute hashtable.
		else{
      logger.debug("Added metadata");
		attributeTable.put("comp_id", this.name);
		attributeTable.putAll(metadataTable);
    //System.out.println("Att Table Values" + attributeTable.toString());
		}
		
	}

	/**
	 * Sets the name for the component. For this name to be used it has to be set before the set method. 
	 * Else the name if present in the file is what will be stored.
   * 
   * @param name the string representing the name of the component 
	 */
	public void setName(String name){
		this.name = name;
	}
	
  /**
   * Sets the connection.
   * 
   * @param conn the connection to be used by the repository component
   */
   public void setConnection(Connection conn){
        this.conn = conn;
    }
	
	 /**
     * Sets the attributes values for the component object's <code>attributeTable</code>. 
     * Updates to the database(component_code) are made only through the component repository's setComponent function.   
	 * @see org.globus.cog.repository.RepositoryComponent#setAttribute(java.lang.String, java.lang.String)
	 */
	public void setAttribute(String attribute, String value) {
		try {
			//check if the attribute is a valid column in the repository
			pStmt = conn.prepareStatement(
					"SELECT attr_id from component_attributes");
			ResultSet rs = pStmt.executeQuery();
			while(rs.next()){
				if(rs.getString("attr_id").equals(attribute)){
					//if it is then add this to the list of attributes.
					attributeTable.put(attribute, value);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
	
	/**
	/* Gets the component code from the database
	 * @see org.globus.cog.repository.RepositoryComponent#get(java.lang.String)
	 */
	public String get(String name) {
    String code = new String(); 
    try {
			pStmt =  conn.prepareStatement(
					"SELECT code from component_code WHERE comp_id like ?");
			pStmt.setString(1, "%" + name + "%");
			ResultSet rs = pStmt.executeQuery();
			while(rs.next()){
				code = rs.getString("code");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return code;
	}
    
	/**
	 * Returns the name of the component
	 * @return the String representing the name of the component
	 **/
    public String getName(){
		return this.name;
    }
    
    /**
     * Returns the code of the component
     * @return the String representing the source of the component
     **/
      public String getCode(){
      return this.code;
      } 
	
    
	/**
	 * Gets the components' attribute values from the database in an enumeration.
	 * @return an enumeration of the attributevalues for the component
	 * @see org.globus.cog.repository.RepositoryComponent#getAttributes()
	 */
	public Enumeration getAttributes() {
		// lists only the attribute desc here.
		Enumeration attrNamesArray = null;
		Vector attList = new Vector();
		String attrName = new String();
		//obtain the list of attributes for the current repository
		 attrNamesArray = compAttributes.listAttributes();
		 
		try {
			pStmt = conn.prepareStatement(
			"SELECT * FROM component_metadata WHERE comp_id = ?");
			pStmt.setString(1, this.name);
			ResultSet rs = pStmt.executeQuery();
			while(rs.next()){	
				while(attrNamesArray.hasMoreElements()){
					attrName = attrNamesArray.nextElement().toString();
					attList.add(rs.getString(attrName));
				}
			}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		return attList.elements();
	}
	
	/** 
	 * Returns the column names for the component_metadata table
	 * @return an enumeration of the attributes for the components of this repository
	 */
	public Enumeration getAttributeColumns(){

		Vector attList = new Vector();
		try {
			pStmt = conn.prepareStatement("SELECT * FROM component_metadata");
			ResultSet rs = pStmt.executeQuery();
			ResultSetMetaData rsMetadata = rs.getMetaData();
			int noOfColumns = rsMetadata.getColumnCount();
			for(int i=1; i <= noOfColumns; i++){
					attList.add(rsMetadata.getColumnName(i));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			logger.debug("Returning column list from getAttributeColumns");
	    return attList.elements();
	}
	
     /**
      * Gets the metadata that is stored in the attribute HashMap that is updated by the user using setAttribute.
      * @return a String with the metadata in a comma-delimited format that will be useful for inserting it to the database using <code>Repository.setComponent()</code> 
      * @see org.globus.cog.repository.RepositoryComponent#getMetadata()
	  */
	public String getMetadata() {
		Enumeration attrNamesArray = null;
		StringBuffer attributeValues = new StringBuffer();
		String attrValue = new String();
		String key = new String();
		logger.debug("Obtaining the metadata");
		//obtain the list of attributes for the current repository
		 attrNamesArray = this.getAttributeColumns();
	    //add only those attributes from the attribute table that are present in the component attributes table. 
		//TODO: change this sometime -  To be able to add new attributes, a new attribute needs to be added to the attribute table first. 
		while(attrNamesArray.hasMoreElements()){
	    key = (String) attrNamesArray.nextElement().toString();
	    key = key.toLowerCase();
      ((DerbyRepositoryAttributes) compAttributes).setConn(this.conn);
	    String attType = compAttributes.get(key);
	    logger.debug("Attribute names: " + key);
	    //checking for the attribute the because the way it gets added to the table varies depending on the type
	    if(attType.startsWith("varchar") || attType.startsWith("char") || attType.startsWith("CLOB") || attType.startsWith("date")){
	    	//if the hashtable contains that attribute then the value is appended
	    	if(attributeTable.containsKey(key)) {
	    		attributeValues.append("'" + attributeTable.get(key) + "'");
	    	}
	    	else{
	    		attributeValues.append("'  '");
	    	}
	    }
	    else if(attType.startsWith("integer") || attType.startsWith("float") || attType.startsWith("decimal") || attType.startsWith("double precision")){
	    	if(attributeTable.containsKey(key)) {
	    		attributeValues.append(attributeTable.get(key));
	    	}
	    	else {
	    		attributeValues.append("  ");
	    	}
	    }
	   logger.debug("Attribute values (ordered) : " + attributeValues.toString());
		if(attrNamesArray.hasMoreElements())attributeValues.append(","); 
		}
		
		return attributeValues.toString();
	}

  /*
   * To implement all methods. But for Derby we require the connection object.
   */
    public void set(String fileName) throws MetaDataNotFoundException {
      set(fileName, null);
    }

}
