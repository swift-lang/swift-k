/*
 * Created on May 27, 2005
 *
 * 
 */
package org.globus.cog.repository.impl.jdbc;

import org.apache.derby.drda.NetworkServerControl;
import org.apache.log4j.Logger;
import org.globus.cog.repository.RepositoryComponent;
import org.globus.cog.repository.Repository;
import org.globus.cog.repository.RepositoryFactory;
import org.globus.cog.repository.RepositoryProperties;

import java.sql.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * The <code>DerbyRepository</code> is an implementation of the <code>Repository</code> interface. 
 * It makes use of jdbc drivers supplied with Apache Derby to connect to both an embedded database as well as a 
 * one on a remote server. 
 * <p>
 * Although, currently the implementation targets the Derby database and is tested on it, in future we will be 
 * supporting other databases since the implementation is being done in JDBC. This class below provides the functionality to connect to a 
 * repository, store components, retrieve components, search for components and load components directly from a file without the use of the RepositoryComponent
 * object. 
 * </p>
 */

public class DerbyRepository implements Repository {
     
  private String hostName;
  private String port;
  private String providerType;
  private String connectionURL;
  public Connection conn;
  public String connId;
  public PreparedStatement pStmt;
  private RepositoryFactory factory;
  private static final String PROPERTIES_FILE = "repository.properties" ;   
  private static final String DERBY_SYSTEM_HOME = "derby.system.home";
  private static final String DERBY_REPOSITORY_DIR = "derby.repository.dir";
  private static final String DERBY_REPOSITORY_TYPE = "derby.repository.type";
  private static final String DERBY_USERNAME = "derby.username";
  private static final String DERBY_PASSWORD = "derby.password";
  private static final String DERBY_HOST = "derby.host";
  private static final String DERBY_PORT = "derby.port";
  private static final String DERBY_LOGFILE = "derby.stream.error.file";
  private static final String DERBY_INFOLOG_APPEND = "derby.infolog.append";
  
  
  private String _derby_system_home;
  private String _derby_repository_dir;
  private String _derby_repository_type;
  private String _derby_username;
  private String _derby_password;
  private String _derby_host;
  private int _derby_port;
  private String _derby_logfile;
  private String _derby_infolog_append;
  
  private static Logger logger = Logger.getLogger(DerbyRepository.class);
	public DerbyRepository(){
      //get the required system properties and the database to be used set values here
	    setProperties();     
	}
    
  /**
   * Gets the system properties from the specified properties file.
   * Is not currently used
   */
    private void setProperties(){
        Properties derbyProps = System.getProperties();
        Properties props = new Properties();
        try {
          props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE));
          //props.load(new FileInputStream(RepositoryProperties.getDefaultPropertiesFile("repository")));
        } catch (IOException e) {
          logger.warn("Was unable to load the properties file and set default values");
          e.printStackTrace();
        }
        this._derby_system_home = props.getProperty(DERBY_SYSTEM_HOME);
        this._derby_repository_dir = props.getProperty(DERBY_REPOSITORY_DIR);
        this._derby_repository_type = props.getProperty(DERBY_REPOSITORY_TYPE);
        this._derby_username = props.getProperty(DERBY_USERNAME);
        this._derby_password = props.getProperty(DERBY_PASSWORD);
        this._derby_host = props.getProperty(DERBY_HOST);
        this._derby_port = Integer.parseInt(props.getProperty(DERBY_PORT));
        this._derby_logfile = props.getProperty(DERBY_LOGFILE);
        this._derby_infolog_append = props.getProperty(DERBY_INFOLOG_APPEND);
        logger.debug("Loaded properties \n");
    }
    
   //Setters and Getters for the Properties 
    
  public void setSystemHome(String systemHome){
         this._derby_system_home = systemHome;
  }
    
   public void setUserName(String user){
     this._derby_username = user;
   }
    
   public void setPassword(String password){
     this._derby_password = password;     
   }
   
   public void setHost(String host){
     this._derby_host = host;
   }
   
   public void setPort(String port){
     this._derby_port = Integer.parseInt(port);
   }
   
   public void setDBLocation(String location){
     this._derby_repository_dir = location;
   }

   public void setRepositoryType(String type){
     this._derby_repository_type = type; 
   }
   
   public void setLogFile(String logFilename) {
     this._derby_logfile = logFilename;
   }
   
   public void setLogAppend(String append) {
     this._derby_infolog_append = append;
   }
   
   public String getUserName(){
     return this._derby_username;
   }
    
   public String getPassword(){
     return this._derby_password;     
   }
   
   public String getHost(){
     return this._derby_host;
   }
   
   public int getPort(){
     return this._derby_port;
   }
   
   public String getDBLocation(){
     return this._derby_repository_dir;
   }

   public String getRepositoryType(){
     return this._derby_repository_type; 
   }
   
   public String getLogFile(){
     return this._derby_logfile;
   }
   
   public String getLogAppend() {
     return this._derby_infolog_append;
   }
   
   
	/**
	 * Sets the connection string to connect to the database. This has replaced the "setProvider" method. 
	 * 
	 * @param providerType the string is used to set a provider for an embedded derby database or a remote database
	 * Options here are "jdbc:local" or "jdbc:remote" 
	 * @param dbLocation the string provides the location of the repository [Local: filesystem location is assigned, Remote: Hostname and portnumber are assigned]
	 * @see org.globus.cog.abstraction.repository.Repository#setProvider(java.lang.String, java.lang.String, java.lang.String)
	 **/
	public void setLocation(String providerType, String dbLocation) {
        this.providerType = providerType;
		if(providerType.equals("local")){
			connectionURL = "jdbc:derby:" + dbLocation;
		}
        if(providerType.equals("remote")){
            //format of the dbLocation should be //<hostname or ip>:<port>/<dbName>
			//eg. "//localhost:1527/workflowRepository"
            connectionURL = "jdbc:derby:"+ dbLocation +";retrieveMessagesFromServerOnGetMessage=true;deferPrepares=true;"; 
        }
  
	}
	
  /**
     * Sets the connection string to connect to the database
     * 
     * @param dbLocation the string provides the location of the repository [Local: filesystem location is assigned, Remote: Hostname and portnumber are assigned]
     * @see org.globus.cog.abstraction.repository.Repository#setProvider(java.lang.String, java.lang.String, java.lang.String)
     **/
    public void setLocation(String dbLocation) {
       connectionURL = dbLocation; 
    }
  
	/**
	 * Returns a connection object that can be used to communicate with the database
	 * 
	 * @return the connection object that holds the connection to the database
	 * @see org.globus.cog.abstraction.repository.Repository#setProvider(java.lang.String, java.lang.String, java.lang.String)
	 **/
	public Connection getLocation(){
		return this.conn;
	}
	
	/**
	 * Connects to the database using the appropriate driver depending on the connection type
	 * 
	 * @see org.globus.cog.abstraction.repository.Repository#connect()
	 **/
	public void connect() {
		try {
            if(_derby_repository_type != null) providerType = _derby_repository_type;
            if(_derby_repository_dir != null) setLocation(providerType, _derby_repository_dir);
            if(providerType.equals(null)) logger.warn("Provider Type needs to be set for connecting");
            
            if(providerType.equals("local")){
				
            // Load the Derby embedded driver class    
			      Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            System.out.println("connection URL : " + connectionURL);
			      conn = DriverManager.getConnection(connectionURL);	
				  
            }
            
            else if(providerType.equals("remote")){
				
             // Load IBM JDBC Universal Driver class
             Class.forName("org.apache.derby.jdbc.ClientDriver");
             /*// Set user and password properties if necessary
             properties.put("user", "APP");
             properties.put("password", "APP");*/
             Properties properties = new Properties();
             properties.put("retrieveMessagesFromServerOnGetMessage", "true");
			  System.out.println("connection URL : " + connectionURL);
		      conn = DriverManager.getConnection(connectionURL);
             // Get a connection
             Connection conn = DriverManager.getConnection(connectionURL); 
			 
            }
            
		} catch (InstantiationException e) {	
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	

	/**
	 * Disconnects from the database closing the open connections and statements
	 * 
	 * @see org.globus.cog.abstraction.repository.Repository#disconnect()
	 **/
	public void disconnect() {
		 try
	        {
	            if (pStmt != null)
	            {
	                pStmt.close();
	            }
	            if (conn != null)
	            {
                conn.close();
	              //  DriverManager.getConnection(connectionURL + ";shutdown=true");
	               
	            }           
	        }
	        catch (SQLException sqlExcept)
	        {
            sqlExcept.printStackTrace();
	        }
	}

	/**
	 * Provides the status of the connection with the database
	 * 
	 * @return Returns true if the connected to the database
	 * @see org.globus.cog.abstraction.repository.Repository#isConnected()
	 **/
	public boolean isConnected() {
		boolean status = false;
		
    try {
			if(!conn.isClosed()) status=true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
    
		return status;
	}
  
  /**
   * Starts the server up at the specified host and port (default is localhost:8080) 
   * @param host String representing the host Ip address
   * @param port portno. default is 8080
   */
  public void startServer(String host, int port) {
    NetworkServerControl server = null;
    OutputStream logInfo = null;
    
    // Localhost ip is set if a value is not provided
    
    try {
      if(host.equals(null) || port ==0) server = new NetworkServerControl();
      else server = new NetworkServerControl(
                                    InetAddress.getByAddress(host.getBytes())          
                                    , port);
      server.start(new PrintWriter(logInfo));
      logInfo.flush();
    } 
    catch (UnknownHostException e1) {
      e1.printStackTrace();
    } 
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Stop the server up at the specified host and port (default is localhost:8080) 
   * @param host String representing the host Ip address
   * @param port portno. default is 8080
   */
  public void stopServer(String host, int port) {
    NetworkServerControl server = null;
    String logInfo = new String();
    
    // Localhost ip is set if a value is not provided
    
    try {
      if(host.equals(null) || port ==0) server = new NetworkServerControl();
      else server = new NetworkServerControl(
                                    InetAddress.getByAddress(host.getBytes())          
                                    , port);
      server.shutdown();
      System.out.println(logInfo);
    } 
    catch (UnknownHostException e1) {
      e1.printStackTrace();
    } 
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Backsup the database to the specified directory. Writes to the database are not allowed 
   * when a backup is being done.
   * @param backupDir
   * @throws SQLException
   */
  public void backUpDatabase(String backupDir)throws SQLException
  {
  CallableStatement cs = conn.prepareCall("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)"); 
  cs.setString(1, backupDir);
  cs.execute(); 
  cs.close();
  System.out.println("Backed up database to "+backupDir);
  }
  
  /**
   * restores the database from the specified directory
   * @param restoreDir
   * @throws SQLException
   */
  public void restoreDatabase(String restoreDir) throws SQLException
  {
    DriverManager.getConnection(connectionURL + ";restoreFrom=" + restoreDir);
    System.out.println("Database restored from backup directory " + restoreDir);
  }
    
  /**
   * Pings the server to check if it is started up 
   * @param host String representing the host address
   * @param port integer representing the portno. 
   * @param ntries integer for the number of tries to connect to the server
   * @return
   */
  public boolean isServerStarted(String host, int port, int ntries)
  {
    for (int i = 1; i <= ntries; i ++)
    {
      NetworkServerControl server = null;
      
      try {
        server = new NetworkServerControl(
                                      InetAddress.getByAddress(host.getBytes())          
                                      , port);
        Thread.sleep(500);
        server.ping();
        return true;
      } 
      catch (UnknownHostException e1) {
        e1.printStackTrace();
      } 
      catch (Exception e) {
        if (i == ntries)
          return false;
      }
    }
    return false;
    
  }
  
  /**
   * Adds a user for a particular database
   * @param username String representing the username 
   * @param password String containing the password for the user
   * @param accessMode String representing mode of acccess: 
   *        fullAccess, readOnlyAccess or noAccess
   * @throws SQLException
   */
  public void addUser(String username, String password, String accessMode) throws SQLException {
    Statement s = conn.createStatement(); 
    
    //  Creating the user database-wide and setting the access mode
    
    s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
      "'derby.user."+ username + "', '" + password + "')"); 

    // Defining read-write users 
    //TODO: check if adding users one by one instead of a list works.
    // otherwise retreive a list of users as below and then add them 
    // along with the new one
    
    s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
    "'derby.database."+ accessMode +"Users', '"+ username +"')"); 

    // Confirming full-access users 
    ResultSet rs = s.executeQuery(
      "VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY(" + 
      "'derby.database.fullAccessUsers')"); 
    rs.next(); 
    System.out.println(rs.getString(1)); 

    s.close(); 
    
    
  }
  
  /**
   * Removes the user for the connected database
   * @param username String representing the username
   * @throws SQLException
   */
  public void removeUser(String username) throws SQLException {
    Statement s = conn.createStatement(); 
    
  
    s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
      "'derby.user."+ username + "', null)"); 

    // Confirming full-access users 
    ResultSet rs = s.executeQuery(
      "VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY(" + 
      "'derby.database.fullAccessUsers')"); 
    rs.next(); 
    System.out.println(rs.getString(1)); 

    s.close(); 
    
    
  }
  
  
  public void setAuthenticationProps(boolean requireAuth) throws SQLException { 
  
    Statement s = conn.createStatement(); 
    
    // Setting and Confirming requireAuthentication 
    s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
    "'derby.connection.requireAuthentication','" + requireAuth + "')");
    ResultSet rs = s.executeQuery( 
      "VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY(" + 
      "'derby.connection.requireAuthentication')"); 
    rs.next(); 
    System.out.println(rs.getString(1)); 
    
    // Setting authentication scheme to Cloudscape 
    // TODO: maybe use some other authentication mechanism
    if(requireAuth == true)
    s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
      "'derby.authentication.provider', 'BUILTIN')"); 
    else if(requireAuth == false)
      s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
        "'derby.authentication.provider', null)");
     
    //  Setting default connection mode to full access
    //TODO: Change this to readonly access for unauthorized users when
    // a full access user has been added to the derby.properties file and tested.
    // (user authorization) 
    s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
      "'derby.database.defaultConnectionMode', 'fullAccess')"); 
    
    // Confirming default connection mode 
    rs = s.executeQuery (
      "VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY(" + 
      "'derby.database.defaultConnectionMode')"); 
    rs.next(); 
    System.out.println(rs.getString(1)); 
    
    s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
    "'derby.user.guest', 'guest')"); 
    
    //TODO: Change this to read only on testing
    s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
    "'derby.database.fullAccessUsers', 'guest')");
    
    //To set property precedence
    //TODO: we would set the following property to TRUE only 
    //when we were ready to deploy.
    s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
    "'derby.database.propertiesOnly', 'false')"); 

  } 

  /**
   * All the settings and properties currently pertaining to the repository 
   * are displayed.
   * @throws SQLException 
   */
  public String repositoryInfo() throws SQLException{
    
    StringBuffer sbuf = new StringBuffer();
    
    sbuf.append("HostName (if not embedded)" + this._derby_host);
    sbuf.append("Port No. (if not embedded)" + this._derby_port);
    sbuf.append("Repository Location" + this._derby_repository_dir);
    sbuf.append("Repository Type" + this._derby_repository_type);

    Statement s = conn.createStatement();
    
    // Obtaining default connection mode 
    ResultSet rs = s.executeQuery (
      "VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY(" + 
      "'derby.database.defaultConnectionMode')"); 
    rs.next(); 
    sbuf.append("derby.database.defaultConnectionMode: " + rs.getString(1)); 
    
    // Obtaining full access users
    rs = s.executeQuery (
      "VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY(" + 
      "'derby.database.fullAccessUsers')"); 
    rs.next(); 
    sbuf.append("derby.database.fullAccessUsers: " + rs.getString(1)); 
    
    //  Obtaining read only access users
    rs = s.executeQuery (
      "VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY(" + 
      "'derby.database.readOnlyAccessUsers')"); 
    rs.next(); 
    sbuf.append("derby.database.readOnlyAccessUsers: " + rs.getString(1)); 
    
    return sbuf.toString();
  }
  
	/**
	 * Stores the component and associated metadata in the repository
	 * 
	 * @param comp the RepositoryComponent Object with the values previously set
	 * @return the boolean returns true if the component has been stored
	 * @see org.globus.cog.abstraction.repository.Repository#setComponent(org.globus.cog.abstraction.repository.RepositoryComponent, java.lang.String)
	 **/
	public boolean setComponent(RepositoryComponent comp, String name) {
		//involves setting component metadata as it doesnt exist and setting the components attributes
		//TODO: right now I am doing this using the client but I want to be able to use an xml parser to do this 
		// automatically without the client having to call component.setAttribute
		boolean status = false;
		logger.debug("In set component: " + comp.getMetadata());
    
		try {

      //checks for duplicate values.
      if(!comp.exists(name)) {      
			//Set the value of the component in the metadata table and the code table
			pStmt = conn.prepareStatement("INSERT INTO component_metadata VALUES("+ comp.getMetadata() +")");

      logger.debug("In set component");
      //	This did not work for updates for some reason.
			//setComponent.setString(1, comp.getMetadata());
		    pStmt.executeUpdate();
			  
		    pStmt = conn.prepareStatement("INSERT INTO component_code VALUES(?, ?)");
        pStmt.setString(1, ((DerbyRepositoryComponent)comp).getName());
        pStmt.setString(2, ((DerbyRepositoryComponent) comp).getCode());
		    pStmt.executeUpdate();
		    conn.commit();
		    status = true;
      }
      else {
        logger.warn("Component "+ name + " already exists in the database" );
      }
		} catch (SQLException e) {
      logger.debug("Was not able to insert the component into the metadata or code tables");
			e.printStackTrace();
		}
		return status;
	}

	/**
	 * Obtains a component with the specified name from the repository
	 * 
	 * @param componentName the string for the component name 
	 * @return a RepositoryComponent object if the component exists in the repository
	 * @see org.globus.cog.abstraction.repository.Repository#getComponent(java.lang.String)
	 **/
	public RepositoryComponent getComponent(String componentName) {
		DerbyRepositoryComponent comp = (DerbyRepositoryComponent) RepositoryFactory.newRepositoryComponent("derby");
    comp.setName(componentName);
    comp.setConnection(conn);
    //check if the component exists in the repository
    if(comp.exists(componentName)) return comp;
    else {
      logger.debug("Component " + componentName + "does not exist in the repository");
      return null;
    }
	}

	/**
	 * removes the component from the repository
	 * 
	 * @param name the string that specifies the component name used to search in the repository
	 * @see org.globus.cog.abstraction.repository.Repository#removeComponent(java.lang.String)
	 **/
	public void removeComponent(String name) {
	  try {
      //TODO: Check if the component actually exists before removing
			//Need to set ON DELETE CASCADE for the tables in database and delete the row from component_metadata
      pStmt = conn.prepareStatement(
      "DELETE FROM component_code WHERE comp_id like ?");
      pStmt.setString(1, "%" + name + "%");
      pStmt.executeUpdate();
      
			pStmt = conn.prepareStatement(
			"DELETE FROM component_metadata WHERE comp_id like ?");
			pStmt.setString(1, "%" + name + "%");
			pStmt.executeUpdate();
		
      logger.debug("Component " + name + "deleted");
		} catch (SQLException e) {
      logger.debug("Was unable to delete component " + name);
			e.printStackTrace();
		}

	}

	/** 
	 * Searches through the repository for the components using a regular expression
   * leaving no value in the value part of the attributevaluepair will list all the existing components 
	 * 
	 * @param attributeValuePair the string pair used to search through the repository
	 * @return the String array containing a list of component names as a result of the search
	 * @see org.globus.cog.abstraction.repository.Repository#search(java.lang.String)
	 */
	public String[] search(String attributeValuePair) {
    
    ArrayList componentNames = new ArrayList();
    String[] strCompArray = new String[100];
    String attribute = new String();
    String value = new String();
    
    // Attribute Value pair may contain any of the attributes present in the repository.
    Enumeration attributeList;
    DerbyRepositoryComponent comp = (DerbyRepositoryComponent) RepositoryFactory.newRepositoryComponent("derby");
    comp.setConnection(conn);
    attributeList = comp.getAttributeColumns();
  
    StringTokenizer st = new StringTokenizer(attributeValuePair, "=");
    while (st.hasMoreTokens()) {
       attribute = st.nextToken().trim();
       if(st.hasMoreTokens()) value = st.nextToken().trim();               
    }
    
    //Testing if the attribute exists for the repository's components
    boolean attributeExists =  false;
    while(attributeList.hasMoreElements()){
      String repAttribute = attributeList.nextElement().toString();
      if(repAttribute.equalsIgnoreCase(attribute)){
        attributeExists = true; 
      }
    }
    
    if(attributeExists)
    {
      ResultSet rs;
      try {
           if((attribute != null) && (value != null )) {
             String query = "SELECT comp_id from component_metadata where " +
                            attribute + " like " + "'%"+ value +"%'";
           /*// value = "%" + value + "%" ; 
            pStmt =  conn.prepareStatement(
            "SELECT comp_id from component_metadata where ? like ?");
            pStmt.clearParameters();
            pStmt.setString(1, attribute);
            pStmt.setString(2, value);*/
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
           }
           // when the value has not been provided for the attribute to allow all values
           else {
             pStmt =  conn.prepareStatement(
             "SELECT comp_id from component_metadata where ? like ?");
             pStmt.setString(1, attribute);
             rs = pStmt.executeQuery();
           }
            
            
            int i = 0;
            while(rs.next()){
                //componentNames.add(rs.getString("comp_id"));
                strCompArray[i] = rs.getString("comp_id");
                //System.out.println("Result set avail " + strCompArray[i]);
                i++;
            }
        
        } catch (SQLException e) {
            logger.debug("Searching for component " + attribute + " like " + value + " failed.");
            e.printStackTrace();
        }
    }
    else {
      logger.debug("The attribute used for search does not exist for the component");
    }
    /*ListIterator itr = componentNames.listIterator();
     String[] strCompArray = null;
     int i = 0;
     System.out.println("Array components");
     while(itr.hasNext()) {
      strCompArray[i] = itr.next().toString();
      System.out.println(strCompArray[i]);
      i++;
     }*/
     if(strCompArray != null) return strCompArray;
     else {
       strCompArray[0] = "No components matched the search criteria";
       return strCompArray;
     }
	} 

	/**
	 * Obtains the components from an XML file and stores them in the repository
	 * 	
	 * @param fileName the string used to specify the location of the file
	 * @see org.globus.cog.abstraction.repository.Repository#loadComponentsFromFile(java.lang.String)
	 **/
	public void loadComponentsFromFile(String fileName) {
    logger.debug("Getting the connection object");
		Connection conn = this.getLocation();
    logger.debug("Creating the repository component");
		DerbyRepositoryComponent comp = (DerbyRepositoryComponent)RepositoryFactory.newRepositoryComponent("derby");
    logger.debug("Obtained an instance of DerbyRepositoryComponent");
    
		try {
      comp.setConnection(conn); 
      logger.debug("Connection was set");
			comp.set(fileName);
		} catch (MetaDataNotFoundException e) {
      logger.debug("Could not extract component metadata from " + fileName);
			e.printStackTrace();
		}
		logger.debug("DerbyRepository.setComponent is being called");
		setComponent(comp, comp.getName());
		}

	/**
	 * Saves the specified component to a file. If the metadata param is set to false then only the code will be transferred to the file. 
	 * The fname of the file is the <componentName>.xml
   * 
	 * @param componentName the string representing the component in the repository
   * @param fileName the string representing the file to store the component in
	 * @return the boolean used to specify if metadata is to be stored in the file  
	 * @see org.globus.cog.abstraction.repository.ComponentRepository#saveComponentsToFile(java.lang.String, java.lang.String, java.lang.boolean)
	 **/
	public void saveComponentsToFile(String componentName, String fileName, boolean metadata) {
        FileOutputStream fout;
        PrintStream p;
        DerbyRepositoryComponent comp =  (DerbyRepositoryComponent) RepositoryFactory.newRepositoryComponent("derby");
        comp.setConnection(conn);
        comp.setName(componentName);
        
        if(comp.exists(componentName)){
          String XMLString = comp.toXML(metadata);
          try {
              fout = new FileOutputStream(fileName);
              p = new PrintStream(fout);
              p.println(XMLString);
              p.close();
          } catch (FileNotFoundException e) {
              logger.debug("Error writing the component to a file");
              e.printStackTrace();
          }
        } 
        else {
          logger.warn("Component " + componentName + "does not exist in the repository" );
        }
	}

}
