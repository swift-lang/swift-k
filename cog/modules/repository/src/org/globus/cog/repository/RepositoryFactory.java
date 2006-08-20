package org.globus.cog.repository;

import org.globus.cog.repository.impl.jdbc.DerbyRepository;
import org.globus.cog.repository.impl.jdbc.DerbyRepositoryAttributes;
import org.globus.cog.repository.impl.jdbc.DerbyRepositoryComponent;

/**
 * An abstract Factory to create the repository without having to 
 * instantiate the implementation class. This is to improve the 
 * extensibility when other types of repositories are implemented.
 */

public class RepositoryFactory {
   
   /**
    * Used to return objects corresponding to specific repository implementations
    * @param repositoryType is the string representing the type of repository
    * @return the <code>Repository</code> object 
    */ 
   public static Repository newRepository(String repositoryType){
       if(repositoryType.equals("derby")) return new DerbyRepository();
       return null;
   }
   
   /**
    * Used to return objects corresponding to specific repositorycomponent implementations
    * @param componentType is the string representing the type of component
    * @return the <code>RepositoryComponent</code> object
    */ 
   public static RepositoryComponent newRepositoryComponent(String componentType){
       if(componentType.equals("derby")) return new DerbyRepositoryComponent();
       return null;
   }
   
   /**
    * Used to return objects corresponding to specific repositoryAttribute implementations
    * @param attributeType is the string representing the type of attributes
    * @return the <code>RepositoryAttributes<object>  
    */ 
   public static RepositoryAttributes newRepositoryAttributes(String attributeType){
       if(attributeType.equals("derby")) return new DerbyRepositoryAttributes();
       return null;
   }
   
}