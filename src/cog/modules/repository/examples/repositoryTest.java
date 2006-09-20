import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;


import org.globus.cog.repository.Repository;
import org.globus.cog.repository.RepositoryComponent;
import org.globus.cog.repository.RepositoryFactory;
import org.globus.cog.repository.impl.jdbc.DerbyRepository;
import org.globus.cog.repository.impl.jdbc.DerbyRepositoryComponent;
import org.apache.derby.tools.ij;
import org.cogkit.repository.util.FileHandler;

/**
 * Creation of a repository 
 */

public class repositoryTest {

	public static String type = "local"; 
	public static String sqlScriptFile = "c:/connect.sql";
	public static String installLocation = "//localhost:1527/c:\\networkedRepository"; 
	public static String REPOSITORY_SCHEMA = "repository_schema_derby.sql";
	public static String TEST_COMPONENT = "c:\\stopwatch.xml";
	
	public static RepositoryFactory factory = null; 
	public static Repository repository;
	public static RepositoryComponent repositoryComponent;
	
	
	public static void connectToRepository(){
		
		//-----------------------------------------------------------------------------
		// Remote server connection string [host, port]
		//-----------------------------------------------------------------------------
		
		
		// -----------------------------------------------------------------------------
		// Connect to an existing remote repository and retrieve components from it. 
		// -----------------------------------------------------------------------------
		
		/**
		 * TODO: Use a properties file to persistently store the database information either 
		 * remote or embedded.
		 */
		
	    Repository repository = RepositoryFactory.newRepository("derby");
	    RepositoryComponent repositoryComponent = RepositoryFactory.newRepositoryComponent("derby");
        
		type = "remote";
		((DerbyRepository) repository).setRepositoryType(type);
		((DerbyRepository) repository).setDBLocation(installLocation);
		((DerbyRepository) repository).connect();	
		
     }
	
	
	public void listComponents(){
		
		
	    String[] compList = repository.search("comp_id=");
	    int i=0;
	    while(compList[i] != null) {
	    System.out.println(compList[i]);
	    i++;
	    }
		
	}
	public void addComponent(){
		
		try{
		 repository.loadComponentsFromFile(TEST_COMPONENT);
         //	If XML File does not have the metadata set Attributes using as below. 
		 // comp.setAttribute("comp_id", "test.xml" ); for the required attribute
	     // Right now this throws an exception but can be changed to remove metadata exception
		
		} 
		catch (Exception e) {
		e.printStackTrace();
		}
		
	}
	
	public void getComponent(){
		
		
	   repositoryComponent = (DerbyRepositoryComponent) repository.getComponent("Stopwatch");
	   ((DerbyRepositoryComponent) repositoryComponent).toFile("tempComp.xml");
		//cRepository.setComponent(comp,comp.getName());
	    String metadata = repositoryComponent.getMetadata();
	    String code = repositoryComponent.get("Stopwatch"); 
		System.out.println("Metadata:" + metadata + "\n" + "Code:" + code + "\n");
		      
		
	}
	
	public void deleteComponent(){
		
	    repository.removeComponent("Stopwatch");  
		
	}
	
	public void disconnectRepository(){

		repository.disconnect();
	}
	
	
	public static void createDBandSchema(String installLocation){
		   
		// -----------------------------------------------------------------
		//Creating a new Repository and creating the schema required for it.
		// -----------------------------------------------------------------
		
		/* Done by creating a script file that is created by appending the 
		 * schema file with the connection scripts and run using ij. Cannot be 
		 * done directly through the repository API since the DDL commands are not 
		 * supported yet within Apache Derby. 
		 */
		
		String connectStr =  "connect 'jdbc:derby:" + installLocation + ";create=true';";
		
		FileHandler fhdlr = new FileHandler();
		try {
			fhdlr.setFile(connectStr.getBytes(),sqlScriptFile);
		} catch (RemoteException e2) {
			e2.printStackTrace();
		}
		
		StringBuffer fileStr = new StringBuffer();
		String line = new String();
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(REPOSITORY_SCHEMA);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		
		try {
			
			line = br.readLine();
			while(line != null) {
				fileStr.append(line + "\n");
				line = br.readLine();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		fhdlr.appendFile(sqlScriptFile, fileStr.toString());
		fhdlr.appendFile(sqlScriptFile,  "disconnect;");
		fhdlr.appendFile(sqlScriptFile, "exit;");
		
		String[] strArray = new String[1];
		strArray[0] = sqlScriptFile;
      
		try {
			
			ij.main(strArray);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    public static void main(String args[]){
		try{
			//createDBandSchema(installLocation);
		    connectToRepository();
		}
		catch(Exception e){
			e.printStackTrace();
		}
      }
	
	
}