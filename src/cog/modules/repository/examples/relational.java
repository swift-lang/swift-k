package org.globus.cog.repository;
/*
 * Created on May 31, 2005
 *
 */


import java.sql.Connection;

import org.apache.log4j.PropertyConfigurator;
import org.globus.cog.repository.Repository;
import org.globus.cog.repository.RepositoryComponent;
import org.globus.cog.repository.RepositoryFactory;
import org.globus.cog.repository.impl.jdbc.DerbyRepository;
import org.globus.cog.repository.impl.jdbc.DerbyRepositoryComponent;
import org.globus.cog.repository.impl.jdbc.MetaDataNotFoundException;

public class relational {
	public static void main(String[] args) {
		DerbyRepository  cRepository = (DerbyRepository) RepositoryFactory.newRepository("derby");
        cRepository.connect();
        Connection conn = cRepository.getLocation();
		DerbyRepositoryComponent comp = (DerbyRepositoryComponent) RepositoryFactory.newRepositoryComponent("derby");
		try {
      //comp.set("c:/stopwatch.xml", conn);
    cRepository.removeComponent("Stopwatch");  
    cRepository.loadComponentsFromFile("c:/stopwatch.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
		//If XML File does not have the metadata set Attributes using as below. 
		// comp.setAttribute("comp_id", "test.xml" ); for the required attribute
        // Right now this throws an exception but can be changed to remove metadata exception
		System.out.println("before setComponent");
    String[] compList = cRepository.search("comp_id=");
    int i=0;
    while(compList[i] != null) {
    System.out.println(compList[i]);
    i++;
  }
    comp = (DerbyRepositoryComponent) cRepository.getComponent("Stopwatch");
    comp.toFile("tempComp.xml");
		//cRepository.setComponent(comp,comp.getName());
    String metadata = comp.getMetadata();
    String code = comp.get("Stopwatch");
    
		cRepository.disconnect();
    System.out.println("Metadata:" + metadata + "\n" + "Code:" + code + "\nDisconnect");
	}
}
