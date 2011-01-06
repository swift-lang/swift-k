package org.globus.cog.test.stresstest;

import java.util.Date;

import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

class JobSubmissionThread extends Thread
{
    private FrequencyFunction myFreqFunc;
    private boolean keepRunning;
    private int numHosts;
    private String[] arrayOfHosts;
    private int[] arrayOfPorts;
    private String[] arrayOfProviders;
    private StressTest mainProgram;
    private TaskHandler[] handlerArray;

    private int thisThreadNumber;
    private static int numberOfThreads = 0;

    private static final int defaultPort = 5243;
    private static final String defaultProvider = "gt2";

    public JobSubmissionThread(StressTest boss, FrequencyFunction aFreqFunc, String[] contactList) {
	int t;
	String[] temp;

	keepRunning = true;
	mainProgram = boss;
	//need a link back to the StressTest object that launched me
	myFreqFunc = aFreqFunc;
	numHosts = contactList.length;
	thisThreadNumber = numberOfThreads ++;

	arrayOfHosts = new String[numHosts];
	arrayOfPorts = new int[numHosts];
	arrayOfProviders = new String[numHosts];
	handlerArray = new TaskHandler[numHosts];

	/*Each string in contactList is a hostname, then a port number, then
	 provider, seperated by a tab. If any are missing the port number or
	 provider, use defaults.*/

	for (t=0; t<numHosts; t++) {
	    temp=contactList[t].split("\t");
	    arrayOfHosts[t] = temp[0];
	    if (temp.length > 1)
		arrayOfPorts[t] = Integer.parseInt(temp[1]);
	    else
		arrayOfPorts[t] = defaultPort;
	    if (temp.length > 2)
		arrayOfProviders[t] = temp[2];
	    else
		arrayOfProviders[t] = defaultProvider;
	}

	/*Set up a seperate TaskHandler for each host, since they might be using
	  different protocols.*/
	for (t=0; t<numHosts; t++) {
	    try {
		handlerArray[t] = AbstractionFactory.newTaskHandler(arrayOfProviders[t]);
	    }
	    
	    catch (ProviderMethodException pme) {
		System.err.println("Provider missing method: "+pme.toString());
		pme.printStackTrace();
	    }
	    catch (InvalidProviderException ipe) {
		System.err.println("Invalid provider: "+ipe.toString());
		ipe.printStackTrace();
		
	    }
	    System.out.println("Will submit jobs to "+arrayOfHosts[t]+" on port "+arrayOfPorts[t]);
	}
    }

    private JobSpecification makeSpec() {
	JobSpecification spec = new JobSpecificationImpl();
        spec.setExecutable("/bin/date");
        spec.setRedirected(true);
        return spec;
    }

    private Service makeService(String hostName, int portNumber, String provider)
	throws InvalidProviderException, ProviderMethodException {

	Service service;
	SecurityContext securityContext;
	ServiceContact sc;

/*Valid providers are:
  gt2, gridftp, webdav, local, ssh, gt3.2, ftp*/

	service = new ServiceImpl(Service.JOB_SUBMISSION);

        service.setProvider(provider.toLowerCase());
	securityContext = AbstractionFactory.newSecurityContext(provider);
	securityContext.setCredentials(null);
	//null means get from my ~/.globus; run grid-proxy-init first.
	service.setSecurityContext(securityContext);

        sc = new ServiceContactImpl();

	//I can set it with a contact (string containing uri)
	//or with a Host and a Port.
	sc.setHost(hostName);
	//sc.setPort(portNumber);

        service.setServiceContact(sc);

	return service;
    }


/*TaskHandler can only handle unsubmitted tasks.  That means I need to create
 new tasks each time.*/
    private Task makeTask(int taskIndex) {
	Task aTask = null;

        try {
	    aTask = new TaskImpl("testTask", Task.JOB_SUBMISSION);
	    aTask.setSpecification(makeSpec());
	    aTask.addService(makeService(arrayOfHosts[taskIndex],
					 arrayOfPorts[taskIndex],
					 arrayOfProviders[taskIndex]));
	    aTask.addStatusListener(mainProgram);
	}
	catch (InvalidProviderException e) {
	    System.err.println("Invalid provider: "+arrayOfProviders[taskIndex]);
	    e.printStackTrace();
	    System.exit(1);
	}
	catch (ProviderMethodException e) {
	    System.err.println("Provider "+arrayOfProviders[taskIndex]+" lacks the proper method.");
	    e.printStackTrace();
	    System.exit(1);
	}
	/*Not possible for task to be null here:*/
	return aTask;
    }
     
    
    private void plotTaskDirectly(Task aTask, boolean succeeded) {
	/*Usually the main program plots something because it recieves a notification
	  event from the submitted task.  However, if we were unable to submit a task
	  we want to plot a zero, and if we're running in debug mode we want to plot
	  ones without really submitting anything.  Thus, this method.*/
	StatusImpl aStatus  =new StatusImpl(succeeded?Status.COMPLETED:Status.FAILED);
	StatusEvent fakeStatusEvent = new StatusEvent(aTask, aStatus);
	mainProgram.statusChanged(fakeStatusEvent);
    }


    private boolean submitTask(Task aTask, TaskHandler theHandler) {

	try {
	    theHandler.submit(aTask);
	} catch (InvalidSecurityContextException ise) {
            System.err.println("Security Exception");
            ise.printStackTrace();
	    return false;
        } catch (TaskSubmissionException tse) {
            System.err.println("TaskSubmission Exception");
            tse.printStackTrace();
	    return false;
        } catch (IllegalSpecException ispe) {
            System.err.println("Specification Exception");
            ispe.printStackTrace();
	    return false;
        } catch (InvalidServiceContactException isce) {
            System.err.println("Service Contact Exception");
            isce.printStackTrace();
	    return false;
        }
	
	return true;
    }


    public void run() {
	long delay;
	int i;
	Task oneTask;

	while (keepRunning) {
	    for (i=0; i<numHosts; i++) {
		System.out.println("User thread "+thisThreadNumber+
				   " submitting a job to " + arrayOfHosts[i] +
				   " using provider " + arrayOfProviders[i] +
				   " on port " + arrayOfPorts[i] +
				   " at "+ new Date());

		try {
		    /*If running in debug mode, don't really submit, just plot true*/
		    oneTask = makeTask(i);
		    if (mainProgram.isRealSubmissionMode() == false)
			plotTaskDirectly(oneTask, true);

		    /*If running for real, submitTask and get true or false back:*/
		    else if (submitTask(oneTask, handlerArray[i])) {
			System.out.println("Submission successful.");
		    }
		    else {
			System.out.println("Couldn't submit.");
			/*Put a zero on the graph if we couldn't submit:*/
			plotTaskDirectly(oneTask, false);
		    }
		    
		}
		catch (Exception e) {
		    System.err.println("Unknown Exception Occurred!" + e);
		    e.printStackTrace();
		    System.exit(1);
		}
	    }
	    try {
		Thread.sleep(myFreqFunc.nextDelay());
	    }
	    catch(Exception e) {
	    }
	}
    }

    public void pleaseStop() {
	keepRunning = false;
    }

    public void restart() {
	myFreqFunc.reset();
	keepRunning = true;
	start();
    }
}
