package org.globus.cog.repository.util;

import java.sql.SQLException;
import java.util.StringTokenizer;

import org.globus.cog.repository.Repository;
import org.globus.cog.repository.RepositoryComponent;
import org.globus.cog.repository.RepositoryFactory;
import org.globus.cog.repository.impl.jdbc.DerbyRepository;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;

public class RepositoryAdminConsole{

  private String type = null;

  private String installLocation = null;
  
  private String host = null;
  
  private int port = 0;
  
  private String user = null;
  
  private String password = null;
  
  private boolean start = false;
  
  private boolean stop = false;
  
  private int ping = 0;
  
  private String logFile = null;
  
  private String logAppend = null;
  
  private boolean authentication = false;
  
  private String backupLocation = null;
  
  private String recoveryLocation = null;
  
  private String addUserName = null; 
  
  private String removeUserName = null;

  private String addPassword = null;

  private String addAccessMode = null;
  
  public void repositoryAdminExecute(){
    RepositoryFactory factory = null; 
    Repository repository = factory.newRepository("derby");
    RepositoryComponent repositoryComponent = factory.newRepositoryComponent("derby");
   
    if(type != null)
        ((DerbyRepository) repository).setRepositoryType(type);
    else
      type =  ((DerbyRepository) repository).getRepositoryType();
    
    if(host != null)
      ((DerbyRepository) repository).setHost(host); 
    else
      host =  ((DerbyRepository) repository).getHost();
    
    if(port != 0)
      ((DerbyRepository) repository).setPort(host); 
    else
      port =  ((DerbyRepository) repository).getPort();
    
    if(installLocation != null)
      ((DerbyRepository) repository).setDBLocation(installLocation); 
    else
      installLocation =  ((DerbyRepository) repository).getDBLocation(); 
    
    if(start)
      ((DerbyRepository) repository).startServer(host, port);
    
    if(stop)
      ((DerbyRepository) repository).stopServer(host, port);

    if(user != null)
      ((DerbyRepository) repository).setUserName(user);
    else
       user =  ((DerbyRepository) repository).getUserName();
    
    if(password != null)
      ((DerbyRepository) repository).setPassword(password);
    else
      password =  ((DerbyRepository) repository).getPassword();
    
    repository.connect();
    
    if(backupLocation != null) {
      try {
        ((DerbyRepository) repository).backUpDatabase(backupLocation);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    else if(recoveryLocation !=null) {
      try {
        ((DerbyRepository) repository).restoreDatabase(backupLocation);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
      
    if(ping != 0)
      ((DerbyRepository) repository).isServerStarted(host, port, ping);
    
    if(logFile != null)
      ((DerbyRepository) repository).setLogFile(logFile);
    else
      logFile = ((DerbyRepository) repository).getLogFile();
    
    if(logAppend != null)
      ((DerbyRepository) repository).setLogAppend(logAppend);
    
    if((addUserName != null) && (addPassword != null))
      try {
        //TODO: May want to change default access mode
        if(addAccessMode == null) addAccessMode = "readOnlyAccess"; 
        ((DerbyRepository) repository).addUser(addUserName, addPassword, addAccessMode);
      } catch (SQLException e) {
        e.printStackTrace();
      }
      
     if(removeUserName != null)
      try {
        ((DerbyRepository) repository).removeUser(removeUserName);
      } catch (SQLException e) {
        e.printStackTrace();
      }
      
     if(authentication)
      try {
        // Authentication provider currently being used is Cloudscape BUILTIN provider
        ((DerbyRepository) repository).setAuthenticationProps(authentication);
      } catch (SQLException e) {
        e.printStackTrace();
      }
  
     
     
  }
  
  public static void main(String args[]){

   ArgumentParser ap = new ArgumentParser();
	ap.setExecutableName("cog-repository-admin");

	ap.addOption("type","Repository Type","type",ArgumentParser.OPTIONAL);
	ap.addAlias("type","t");

	ap.addOption("install", "copy database to user specified location","location",ArgumentParser.OPTIONAL);
	ap.addAlias("install","i");

	ap.addOption("host","host name","host",ArgumentParser.OPTIONAL);
	ap.addAlias("host","h");

	ap.addOption("port","port to use","port",ArgumentParser.OPTIONAL);
	ap.addAlias("portnumber","pn");

	ap.addOption("username","Required for authentication","username",ArgumentParser.OPTIONAL);
	ap.addAlias("username","u");

	ap.addOption("password","Required for authentication","password",ArgumentParser.OPTIONAL);
	ap.addAlias("password","pd");

	ap.addFlag("start","Starts Repository Server");
	ap.addAlias("start","st");

	ap.addFlag("stop","Stops Repository Server");
	ap.addAlias("stop","sp");

	ap.addOption("backup","Backup location","string",ArgumentParser.OPTIONAL);
	ap.addAlias("backup","b");

	ap.addOption("recovery","Reverts up to backup location", "string", ArgumentParser.OPTIONAL);
	ap.addAlias("recovery","r");

	ap.addOption("ping","Tests to see if server is running(No. of tries)","string", ArgumentParser.OPTIONAL);
	ap.addAlias("ping","p");

  ap.addOption("logfile","Error filename (Default: derby.log)","string",ArgumentParser.OPTIONAL);
  ap.addAlias("logfile","l");
  
  ap.addOption("logappend","Append to existing log(Static Property - Reboot required)","string",ArgumentParser.OPTIONAL);
  ap.addAlias("logappend","la");    

	ap.addOption("add-user","Add user to database (username,password,fullAccess/readOnlyAccess/noAccess", "string",ArgumentParser.OPTIONAL);
	ap.addAlias("add-user","a");

	ap.addOption("remove-user","remove user","string",ArgumentParser.OPTIONAL);
	ap.addAlias("remove-user","r");

	ap.addOption("authentication", "true/false toggles user authentication","string",ArgumentParser.OPTIONAL);
	ap.addAlias("authentication","a");

        try {
            ap.parse(args);
            if (ap.isPresent("help")) {
                ap.usage();
             } 	 
            else {
                ap.checkMandatory();
                try {
                   RepositoryAdminConsole adminConsole = new RepositoryAdminConsole();
		   if(ap.isPresent("type"))	
		  	 adminConsole.setType(ap.getStringValue("type")); 
	
		   if(ap.isPresent("install"))
	  		 adminConsole.setInstall(ap.getStringValue("install"));

		   if(ap.isPresent("host"))
		   	adminConsole.setHost(ap.getStringValue("host"));

		   if(ap.isPresent("portnumber")) 
			  adminConsole.setPort(ap.getStringValue("port"));

		   if(ap.isPresent("username"))
		   	adminConsole.setUser(ap.getStringValue("username"));
		   
		   if(ap.isPresent("password"))
		 	adminConsole.setPassword(ap.getStringValue("password"));

		   if(ap.isPresent("start"))
			adminConsole.start();
		   	
		   if(ap.isPresent("stop"))
			adminConsole.stop();
		
		   if(ap.isPresent("backup"))
			adminConsole.setBackupLocation(ap.getStringValue("backup"));
		
		   if(ap.isPresent("recovery"))
			adminConsole.setRecoveryLocation(ap.getStringValue("recovery"));
		  
		   if(ap.isPresent("ping"))
			adminConsole.ping(ap.getIntValue("ping"));

		   if(ap.isPresent("logfile"))
			adminConsole.setLogFile(ap.getStringValue("logfile"));
       
       if(ap.isPresent("logappend"))
        adminConsole.setLogAppend(ap.getStringValue("logAppend"));

		   if(ap.isPresent("add-user"))
			adminConsole.addUser(ap.getStringValue("add-user"));

		   if(ap.isPresent("remove-user"))
			adminConsole.removeUser(ap.getStringValue("remove-user"));

		   if(ap.isPresent("authentication"))	
			adminConsole.setAuthentication(ap.getStringValue("authentication"));
			

		} catch (Exception e) {
			System.err.println("Error parsing arguments: "+e.getMessage());
                }
            }
        } catch (ArgumentParserException e) {
            System.err.println("Error parsing arguments: " +e.getMessage());
            ap.usage();
        }


	}




public void setType(String type){
	this.type = type;
}

public void setInstall(String installLocation){
	this.installLocation = installLocation;
}

public void setHost(String host){
	this.host = host;
}

public void setPort(String port){
	this.port = Integer.parseInt(port);
}

public void setUser(String userName){
	this.user = userName;
}

public void setPassword(String password){
  this.password = password;
}

public void start(){
	this.start = true;
}

public void stop(){
	this.stop = true;
}

public void setBackupLocation(String backupLocation){
	this.backupLocation = backupLocation;
}

public void setRecoveryLocation(String recoveryLocation){
	this.recoveryLocation = recoveryLocation;
}

public void ping(int ntries){
	this.ping = ntries;
}

public void setLogFile(String errorFilename){
	this.logFile = errorFilename;
}

public void setLogAppend(String append){
  this.logAppend = append;
}

public void addUser(String userInfo){
  StringTokenizer st = new StringTokenizer(userInfo, ",");
  while (st.hasMoreTokens()) {
    addUserName = st.nextToken().trim();
    addPassword = st.nextToken().trim();   
    addAccessMode = st.nextToken().trim();
   }
}

public void removeUser(String user){
  this.removeUserName = user;
}

public void setAuthentication(String authentication){
  if(authentication.equalsIgnoreCase("true"))
    this.authentication = true;
  if(authentication.equalsIgnoreCase("false"))
    this.authentication = false;
}

}



