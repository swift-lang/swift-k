import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;

import org.globus.cog.repository.Repository;
import org.globus.cog.repository.RepositoryComponent;
import org.globus.cog.repository.RepositoryFactory;
import org.globus.cog.repository.impl.jdbc.DerbyRepository;
import org.apache.derby.tools.ij;
import org.cogkit.repository.util.FileHandler;

/**
 * Creation of a repository 
 */

public class repositoryTest {

	public static String type = "local"; 
	public static String sqlScriptFile = "c:/connect.sql";
	public static String installLocation = "c:/KTest"; 
	
    public static void main(String args[]){ 
		/*
		RepositoryFactory factory = null; 
	    Repository repository = RepositoryFactory.newRepository("derby");
	    RepositoryComponent repositoryComponent = RepositoryFactory.newRepositoryComponent("derby");

		((DerbyRepository) repository).setRepositoryType(type);
		((DerbyRepository) repository).setDBLocation(installLocation);
	    //((DerbyRepository) repository).setLocation(installLocation); 
		((DerbyRepository) repository).connect();*/
		
		//Running a script using ij
		
		//strArray[0] = "connect 'jdbc:derby:" + installLocation + "';create=true";
	
		String connectStr =  "connect 'jdbc:derby:" + installLocation + ";create=true';";
		
		FileHandler fhdlr = new FileHandler();
		try {
			fhdlr.setFile(connectStr.getBytes(),sqlScriptFile);
		} catch (RemoteException e2) {
			e2.printStackTrace();
		}
		
		StringBuffer fileStr = new StringBuffer();
		String line = new String();
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("repository_schema_derby.sql");
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
       //strArray[2] = "disconnect";

		
		try {
			
			ij.main(strArray);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
     }
	
}