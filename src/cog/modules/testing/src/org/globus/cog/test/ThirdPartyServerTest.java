
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

import org.apache.log4j.Logger;
import org.globus.io.urlcopy.UrlCopy;
import org.globus.util.GlobusURL;



public class ThirdPartyServerTest{

    String testingServer = null;
    String thirdPartyServer = null;
    String testServer = null;
    private static Logger logger =
        Logger.getLogger(ThirdPartyServerTest.class.getName());

     public ThirdPartyServerTest (String testingServer,
				 String thirdPartyServer){

	

	this.testingServer = testingServer;
	this.thirdPartyServer = thirdPartyServer;

    }

    public static void main(String args[]){

	ThirdPartyServerTest test = new ThirdPartyServerTest(args[0],args[1]);

	if(test.testServer()){
	    System.out.println("The server "+ test.getTestServerName()+
			       " has third party support.");
	}else{
	    System.out.println("\n\nRESULT : The server "+ test.getTestServerName()+
			       " does not have third party support.\n");
	}

    }

    public boolean testServer() {
	GlobusURL from = null;
	GlobusURL to = null;
	try{
	    from = new GlobusURL(testingServer);
	    to  = new GlobusURL(thirdPartyServer);
	}catch(Exception e){
	    
	}
	setTestServerName(from.getHost());
    	if(testUrlCopy(from, to,true)){
	    System.out.println("Passed the test with thridParty feature supported");
	    return true;
	}else if (testUrlCopy(from, to,false)){
	    System.out.println("\nPassed the test with thridParty feature not supported\n");
	    return false;
	}else{
	    System.out.println("Failed with and without thridParty feature supported");
	    return false;
	}
	
    }
    
    public  boolean testUrlCopy(GlobusURL from, GlobusURL to, boolean thirdPartySupport){
	System.out.println("\nTESTING THIRD PARTY USING URLCOPY\n");
	System.out.println("\nFROM : "+from +"\nTO :" +to);
	try {

	    UrlCopy uc = new UrlCopy();
	    uc.setSourceUrl(from);
	    uc.setDestinationUrl(to);
	    uc.setUseThirdPartyCopy(thirdPartySupport);
	    uc.copy();
	    System.out.println("\nSuccessfully done :URLCOPY passed the test\n");   
	    return true;
	} catch(Exception e) {
	    System.err.println("\nURLCOPY failed :GlobusUrlCopy error: " + e.getMessage());
	    return false;
	}
    }
    public String getTestServerName(){
	return testServer;
    }
    public void setTestServerName(String testServer){
	this.testServer = testServer;
    }

}

