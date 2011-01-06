package org.globus.cog.monitor.guss;

import org.globus.cog.monitor.guss.subcreated.GUSSSoapBindingStub;
import org.globus.cog.monitor.guss.subcreated.GUSSIF;
import org.globus.cog.monitor.guss.subcreated.GUSSIFServiceLocator;
import java.net.URL;
import java.util.LinkedList;
import java.rmi.RemoteException;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Date;
import java.util.Calendar;

/**
 * A simple client which invokes {@link GUSSImpl} through its web
 * service interface.  Can be run from the command line or used as
 * glue for a JSP or portlet.
 * @version 1.8
 */
public class GUSSClient implements GUSSConstants
{
    private GUSSIF theStub;

    /**
     *This no-argument constructor attempts a connection to the GUSS {@link GUSSImpl service}
     *using the hostname and portnum from the file guss.properties*/
    public GUSSClient() {
	//No-arg constructor gets hostname and portnum from guss.properties
	String serviceHostName;
	int servicePort;

	try {
	    Properties p = new Properties();
	    InputStream fis=GUSSClient.class.getResourceAsStream("/guss.properties");
	    p.load(fis);
	    serviceHostName = p.getProperty("service-hostname");
	    servicePort = Integer.parseInt(p.getProperty("service-port"));
	    contactTheService(serviceHostName, servicePort);
	} catch (FileNotFoundException e) {
	    theStub = null;
	} catch (IOException e) {
	    theStub = null;
	}
    }

    /**
     *This constructor attempts a connection to the GUSS {@link GUSSImpl service}
     *@param hostname name of the host to attempt a connection to
     *@param portNum name of the port to attempt connecting on*/
    public GUSSClient(String hostname, int portNum) {
	contactTheService(hostname, portNum);
    }

    /**
     *Helper function used by both constructors to contact the GUSS {@link GUSSImpl service}
     *@param hostname name of the host to attempt a connection to
     *@param portNum name of the port to attempt connecting on*/
    private void contactTheService(String hostname, int portNum) {
	URL serviceLoc;
	GUSSIFServiceLocator gopher = null;

	try {
	    gopher = new GUSSIFServiceLocator();
	    if (gopher == null) {
		System.err.println("Couldn't create GUSSIFServiceLocator.");
		theStub = null;
		return;
	    }
	    serviceLoc = new URL("http://" + hostname + ":"+portNum
				 +"/axis/services/GUSS");
	    theStub= gopher.getGUSS(serviceLoc);
	    if (theStub == null) {
		System.err.println("Couldn't getGUSS.");
		theStub = null;
		return;
		}
	}
	catch (Exception e) {
	    System.err.println(e.toString());
	    theStub = null;
	    return;
	}
    }

    /**
     *Invokes {@link GUSSImpl#makeGraphImage makeGraphImage} on GUSSImpl through the
     *web service interface.  Parameters are the same.
     *@return the return value from GUSSImpl.makeGraphImage if successful; an error message otherwise.*/
    public String makeGraphImage(int scope, int quant, String[] args) {
	String thePngURI;
	
	if (theStub == null) {
	    return "Can't find web service.";
	}

	try {
	    thePngURI = theStub.makeGraphImage(scope, quant, args);
	    if (thePngURI == null) 
		return "Web service returned null.";
	    else 
		return thePngURI;
	}
	catch (RemoteException e) {
	    Throwable innerException = e.getCause();
	    String errorMessage = "Web service had remote error:" + e.getMessage();
	    if (innerException != null) {
		innerException = e.getCause();
		errorMessage += "Cause was: " + innerException.getMessage();
	    }
	    return errorMessage;
	}
    }

    public String makeGraphImage(int quant, String granularity, Date startDate, Date endDate) {
	
	String[] arguments = new String[4];
	arguments[0] = Long.toString(startDate.getTime());
	arguments[1] = Long.toString(endDate.getTime());
	arguments[2] = "hour";
	
        return makeGraphImage(1, quant, arguments);
    }

    /**
     *Invokes {@link GUSSImpl#getAllHostNames getAllHostNames} on GUSSImpl through the
     *web service interface.  Parameters are the same.
     *@return the return value from GUSSImpl.makeGraphImage if successful; an error message otherwise.*/
    public String getAllHostNames(String[] args) {
	String returnThis;
	int i;

	if (theStub == null) {
	    return "Can't find web service.";
	}

	try {
	    
	    returnThis = theStub.getAllHostNames(args);
	    
	    if (returnThis == null) 
		return "Web service returned null.";
	    else {
		return returnThis;
	    }
	}
	catch (RemoteException e) {
	    e.printStackTrace();
	    return "Web service had remote error: "+e.getMessage();
	}
    }

    /**
     *Invokes {@link GUSSImpl#makeTable makeTable} on GUSSImpl through the
     *web service interface.  Parameters are the same.
     *@return the return value from GUSSImpl.makeGraphImage if successful; an error message otherwise.*/
    public String makeTable(int scope, int quant, String[] args) {
	String theTable;
	
	if (theStub == null) {
	    return "Can't find web service.";
	}

	try {
	    theTable = theStub.makeTable(scope, quant, args);
	    if (theTable == null) 
		return "Web service returned null.";
	    else 
		return theTable;
	}
	catch (RemoteException e) {
	    e.printStackTrace();
	    return "Web service had remote error.";
	}
    }

    /**
     *This main function is irrelevant when GUSSClient is used as glue for a JSP or portlet.
     *It exists as a way to run tests from the command line.
     *@param args standard command-line arguments.  Expects two arguments: a hostname and a port number to connect to.*/
    public static void main(String args[]) {
	String hostname;
	int port;
	GUSSClient me;
	String imageLoc;
	String[] arguments = new String[3];

	Date startDate, endDate;
	Calendar cal = Calendar.getInstance();

	//args[0] should be name of host, and args[1] port number.
	if (args.length < 2) {
	    System.err.println("Usage: GUSSClient hostname portnum");
	    System.exit(-1);
	}

	hostname = args[0];
	port = Integer.parseInt(args[1]);

	me = new GUSSClient(hostname, port);


	if (args.length >= 3 && args[2].equals("--report")) {
	    System.out.println("Generating full database report.  This will take a few minutes.");
	    System.out.println(me.makeTable(1, 1, args));
	}
	else {
	    System.out.println("Testing GUSS.  Running on localhost port "+port);

	    endDate = cal.getTime();
	    cal.add(Calendar.DATE, -30);
	    startDate = cal.getTime();
	    
	    arguments[0] = Long.toString(startDate.getTime());
	    arguments[1] = Long.toString(endDate.getTime());
	    arguments[2] = "hour";
	    
	    System.out.println("Here is graph of number of transfers over the past three weeks:");
	    System.out.println(me.makeGraphImage(1, NUM_TRANSFERS, arguments));

	    /*	    endDate = cal.getTime();
	    cal.add(Calendar.DATE, -21);
	    startDate = cal.getTime();
	    System.out.println("Here is graph of number of hosts over the past three weeks:");
	    System.out.println(me.makeGraphImage(NUM_HOSTS, "hour",
						 startDate, endDate));
	    System.out.println("Here is graph of transfer volume over the past three weeks:");
	    System.out.println(me.makeGraphImage(TRANSFER_VOLUME, "hour",
						 startDate, endDate));
	    System.out.println("Here is graph of number of transfers over the past three weeks:");
	    System.out.println(me.makeGraphImage(NUM_TRANSFERS, "hour",
	    startDate, endDate));*/
	}

    }
}

