/*
 * 
 */
package org.globus.cog.repository;

import java.io.File;
import java.net.URI;

import org.apache.log4j.Logger;

/**
 * 
 */
public class RepositoryProperties{

	private static Logger logger = Logger.getLogger(RepositoryProperties.class);

	private static RepositoryProperties defaultProperties;

	/**
	 * 1) Starts searching the classpath for ../etc/<identifier>.properties
	 * 2) returns globus.home/<identifier>.properties
	 * @return
	 */
	public static File getDefaultPropertiesFile(String identifier) {
	    String fileName = System.getProperty(identifier+".configuration");
	    if(fileName!=null) {
	        return new File(fileName);
	    }
	    
		String classPath = System.getProperty("java.class.path");
		String pathSeparator = System.getProperty("path.separator");
    String userDir = System.getProperty("user.dir");
    logger.debug("Class path: " +  classPath + "\n path separator: " + pathSeparator);
    logger.debug("User Dir: " + userDir);
		if(classPath != null && pathSeparator!=null) {
			String[] paths = classPath.split(pathSeparator);
      File file = new File(userDir);
///// added this now - 10-12-05      
// try and find the <identifier>.properties in ../etc/<identifier>.properties
/*try {
if(file.isDirectory()) {
  file = file.getParentFile();
}else if (file.isFile()) {
  file = file.getParentFile().getParentFile();
}
//        URI gshURI = new URI(file.getAbsoluteFile().toURI()+"/etc/"+identifier+".properties");
URI gshURI = new URI(file.getAbsoluteFile().toURI()+"../examples/repository/components/"+identifier);

File repositoryProp = new File(gshURI);
if(repositoryProp.exists() && repositoryProp.isFile()) {            
  return repositoryProp;
}else {
  logger.debug("Didn't find "+identifier+".properties at '"+repositoryProp+"'");
  return null;
}
}catch(Exception exception) {
logger.debug("Didn't find "+identifier+".properties relative ",exception);
}


*/

/////--------------------------------  actual code
			for(int i=0;paths!=null && i<paths.length;i++) {
				 file = new File(paths[i]);
								
				// try and find the <identifier>.properties in ../etc/<identifier>.properties
				try {
					if(file.isDirectory()) {
						file = file.getParentFile();
					}else if (file.isFile()) {
						file = file.getParentFile().getParentFile();
					}
	//				URI gshURI = new URI(file.getAbsoluteFile().toURI()+"/etc/"+identifier+".properties");
          URI gshURI = new URI(file.getAbsoluteFile().toURI()+"/etc/"+identifier+".properties");
					File repositoryProp = new File(gshURI);
					if(repositoryProp.exists() && repositoryProp.isFile()) {						
						return repositoryProp;
					}else {
						logger.debug("Didn't find "+identifier+".properties at '"+repositoryProp+"'");
            return null;
					}
				}catch(Exception exception) {
					logger.debug("Didn't find "+identifier+".properties relative to '"+paths[i]+"'",exception);
				}
			}
		}else {
			  logger.warn("couldn't get either classPath='" + classPath
					+ "' or path separtor='" + pathSeparator
				  + "' to determine properties for "+identifier);
      return null;
		}
    return null;
	}
	
}
