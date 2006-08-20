package org.globus.cog.repository.util;

import java.util.StringTokenizer;

import org.globus.cog.repository.RepositoryFactory;
import org.globus.cog.repository.impl.jdbc.DerbyRepository;
import org.globus.cog.repository.impl.jdbc.DerbyRepositoryComponent;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;

public class RepositoryConsole{
   
  private String type = null;

  private String dblocation = null;
  
  private String addComponentFile = null;
  
  private String removeComponentName = null;
  
  private boolean listComponent = false;

  private boolean createRepository = false;
  
  private String searchComponentString = null;
  
  private String saveComponentName = null;

  private String saveFileName = null;
  
  private String getComponentName = null;


  
  public void repositoryExecute(){
    
    RepositoryFactory factory = null; 
    
    DerbyRepository repository = (DerbyRepository) RepositoryFactory.newRepository("derby");
    DerbyRepositoryComponent repositoryComponent = (DerbyRepositoryComponent) RepositoryFactory.newRepositoryComponent("derby");
    
    
    if(type != null) {
    ((DerbyRepository) repository).setRepositoryType(type);
    }
    else {type = "local";}
    
    if(dblocation != null) {
      ((DerbyRepository)repository).setDBLocation(dblocation);
    }
    
    if(createRepository != false){
      // call the connect statement to with diff connection URL "create=true"
      // call loadcomponents from file to load from createRepository.xml       
    }
    
    repository.connect();
    repositoryComponent.setConnection(repository.conn);
    
    
    if(addComponentFile != null){
        repository.loadComponentsFromFile(addComponentFile);
    } 
    
    if(getComponentName != null){
      if(repositoryComponent.exists(getComponentName)){
        String comp = repositoryComponent.get(getComponentName);
        System.out.println("Component " + getComponentName + "\n" + comp);
      }
      else{
        System.err.println("Component Does not exist in the repository");
        System.out.println("Component" + getComponentName + "does not exist in the repository");    
      }
    }
    
    if(removeComponentName != null){
      if(repositoryComponent.exists(removeComponentName)){
        repository.removeComponent(removeComponentName);
      }
      else{
        System.err.println("Component does not exist in the repository");
        System.out.println("Component" + removeComponentName + "does not exist in the repository");    
      }
    }
    
    if(listComponent != false){
      String[] compList = repository.search("comp_id=");
      int i = 0;
      System.out.println("List of available components \n");
      while(compList[i] != null) {
        System.out.println(compList[i]);
        i++;
      }
      
      System.out.println("Total no. of components : " + i);
    }
    
    if(searchComponentString != null){
      String[] compList = repository.search(searchComponentString);
      int i = 0;
      System.out.println("Matching component(s) \n");
      while(compList[i] != null) {
        System.out.println(compList[i]);
        i++;
      }
    }
    
    if((saveComponentName != null ) && (saveFileName != null)){
      // Added "true" to save along with the metadata
      repository.saveComponentsToFile(saveComponentName, saveFileName, true);
    }
    
    repository.disconnect();
  }
     
  public static void main(String args[]){
	  ArgumentParser ap = new ArgumentParser();
    
    // default repository-type(local/remote) is obtained from properties file 
    // if not specified 
  	ap.addOption("repository-type","type of repository","repository-type",
		     ArgumentParser.OPTIONAL);
	  ap.addAlias("repository-type","rt");

    //If location is not specified this will be picked up from the properties file
  	ap.addOption("dblocation","location for the derby system","location"
		     ,ArgumentParser.OPTIONAL);
	  ap.addAlias("dblocation","db");
    
    ap.addFlag("createLocalRepository","Create a new local repository " +
        " (dbLocation also needs to be specified to create a new repository)");
    ap.addAlias("createLocalRepository","cr");

	  ap.addOption("addComponent","Add component to the repository","name"
		     ,ArgumentParser.OPTIONAL);
	  ap.addAlias("addComponent","ac");
    
    ap.addOption("getComponent","get the component from the repository","name"
         ,ArgumentParser.OPTIONAL);
    ap.addAlias("getComponent","gc");

    ap.addOption("removeComponent","Remove component from the repository",
		     "name",ArgumentParser.OPTIONAL);
    ap.addAlias("removeComponent","rc");	

    ap.addOption("searchComponent","Search components in the repository",
		     "search string[attribute value pair]",ArgumentParser.OPTIONAL);
    ap.addAlias("searchComponent","sc");

    ap.addOption("saveComponent","Save component to a file in the repository",
		     "name",ArgumentParser.OPTIONAL);
    ap.addAlias("saveComponent","fc");

    ap.addFlag("listComponent","List components in the repository");
    ap.addAlias("listComponent","lc");

	  ap.addFlag("help", "Display usage: Some of the commands are mutually exclusive with respect to others");
	  ap.addAlias("help", "h");

    try {
         ap.parse(args);
         if (ap.isPresent("help")) {
            ap.usage();
         } 	 
         else {
         ap.checkMandatory();
          
         RepositoryConsole console = new RepositoryConsole();

	    	 if(ap.isPresent("type"))
			     console.setType(ap.getStringValue(
			     "type"));
         
         if(ap.isPresent("dblocation"))
		     console.setDBLocation(ap.getStringValue("dblocation"));
		   
         if(ap.isPresent("createLocalRepository"))
           console.createRepository(ap.isPresent("createLocalRepository"));
         
		     if(ap.isPresent("addComponent"))
			   console.addComponent(ap.getStringValue
					     ("addComponent"));
         
         if(ap.isPresent("getComponent"))
           console.getComponent(ap.getStringValue
                 ("getComponent"));
         
		  
		     if(ap.isPresent("removeComponent"))
			   console.removeComponent(ap.getStringValue
						("removeComponent"));		   
		   
         
		     if(ap.isPresent("listComponent"))
			   console.listComponents(ap.isPresent("listComponent"));
		  
         
		     if(ap.isPresent("searchComponent"))
			   console.searchComponents(ap.getStringValue
						("searchComponent"));
         
		     if(ap.isPresent("saveComponent"))
			   console.saveComponent(ap.getStringValue("saveComponent"));
      
         console.repositoryExecute();
         }
      }
      
      catch (ArgumentParserException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            e.printStackTrace();
            ap.usage();
        }
      catch (Exception e) {
        System.err.println("Error: "+
               e.getMessage());
        e.printStackTrace();
        }

	}

public void setType(String type){
	System.out.println("Type: "+type);
}


public void setDBLocation(String dblocation){
  this.dblocation = dblocation;
	System.out.println("Database Location: "+dblocation);
}

private void createRepository(boolean b) {
  this.createRepository = b;
  }

public void addComponent(String fileName){
  this.addComponentFile = fileName; 
}

public void getComponent(String componentName){
  this.getComponentName = componentName;
}

public void removeComponent(String componentName){
	this.removeComponentName = componentName;
}

public void listComponents(boolean list){
  this.listComponent = list;
}

public void searchComponents(String attributeValuePair){
	this.searchComponentString = attributeValuePair;
}

public void saveComponent(String saveArgs){

  StringTokenizer st = new StringTokenizer(saveArgs, ",");
  while (st.hasMoreTokens()) {
     saveComponentName = st.nextToken().trim();
     saveFileName = st.nextToken().trim();               
   }
}

}
