package org.globus.cog.test.stresstest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;

import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;

public class StressTest implements StatusListener
{
    //used if --load option:
    private BufferedReader logInput;
    //Used if a file of host names is given:
    private String[] hostNameList;
    //Used otherwise:
    private String hostName;
    private int port;
    private String provider;

    private String freqFuncName;
    private UsersFunction userFunc;
    private GraphResultsFrame theWindow;
    private LinkedList runningThreadList;
    private LinkedList sleepingThreadList;
    private boolean reallySubmitAnything;
    private boolean constantScale;
    private FileWriter logWriter;

    private final String DEFAULT_FREQUENCY = "constant";
    private final String DEFAULT_USERS = "constant";
    private final String DEFAULT_HOSTFILE = "hostlist.txt";
    private final String DEFAULT_LOGFILE = "stressTestLog.txt";
    private final String DEFAULT_COMMAND = "/bin/date";
    private final int DEFAULT_PORT = 5243;
    private final String DEFAULT_PROVIDER = "gt2";

    //FIXME:  The following two values should be user-settable.
    private final long USER_WAIT_TIME = 60000; 
    //wait one minute before rechecking users function
    private final long DEFAULT_RANGE = 100000;
    //Range of 100 seconds on the horizontal axis


    public StressTest() {
	reallySubmitAnything = true;
	constantScale=false;
	runningThreadList = new LinkedList();
	sleepingThreadList = new LinkedList();
	logInput = null;
    }

    //FIXME -- Clarify this:
    private void printUsage() {
	    System.out.println("java org.globus.cog.StressTest");
	    System.out.println("--help      Display this message and quit.");
	    System.out.println("--load <file>     Display old logfile contents.");
	    System.out.println("-n     Don't actually submit any jobs (Debug mode).");
	    System.out.println("-c     Constant scale graph.");
	    System.out.println("-L <file>    Output filename for logging.");
	    System.out.println("-h <hostname>    The host to submit jobs to.");
	    System.out.println("-h <file>     A file to read a list of hostnames from.");
	    System.out.println("-p <number>    The port to connect on (used only if a sitename is given).");
	    System.out.println("-P <provider>    The Provider to use, for example gt2, gt3.2, or ssh.  (Used only if a sitename is given)");
	    System.out.println("-f <constant|exponential|random>    A frequency function (default constant).");
	    System.out.println("-u <constant|linear|exponential|random>    A number of users function (default constant).");
    }


    public static FrequencyFunction makeFreqFunc(String functionName) {
	FrequencyFunction freqFunc = null;

	if (functionName.equals("constant"))
	    freqFunc = new ConstantFrequency(10);
	else if (functionName.equals("exponential"))
	    freqFunc = new ExponentialFrequency(20, 1.2);
	else if (functionName.equals("random"))
	    freqFunc = new RandomFrequency(20, 10);
	else {
	    System.err.println("There is no function called "+functionName);
	    System.exit(-1);
	}
	
	System.out.println("Using frequency function "+functionName);
      	return freqFunc;
    }	

    private boolean useUsersFunction(String functionName) {
	if (functionName.equals("constant"))
	    userFunc = new ConstantUsers(1);
	else if (functionName.equals("exponential"))
	    userFunc = new ExponentialUsers(1, 2);
	else if (functionName.equals("random"))
	    userFunc = new RandomUsers(10, 10);
	else if (functionName.equals("linear"))
	    userFunc = new LinearUsers(1, 1);
	else {
	    System.err.println("There is no function called "+functionName);
	    System.exit(-1);
	}
	
	System.out.println("Using users function "+functionName);
       	return true;
    }

    private boolean loadHostListFile(String fileName) {
	FileReader hostFile;
	long length;
	int fileSize;
	char[] buffer;
	String omniString;
	
	try {
	    length = new File(fileName).length();
	    hostFile = new FileReader(fileName);
	    
	    fileSize = (int)length;
	    buffer = new char[fileSize];
	    hostFile.read(buffer, 0, fileSize);
	    omniString = new String(buffer, 0, fileSize);
	    hostNameList = omniString.split("\n");
	    hostFile.close();
	}
	catch (FileNotFoundException e) {
	    System.err.println("There is no file named "+fileName);
	    return false;
	}
	catch (IOException e) {
	    System.err.println("IO error occurred: "+e);
	    return false;
	}

	System.out.println("Using host list file "+fileName);
	return true;
    }


    //FIXME -- Replace this with the CoGKit's Argument Parser
    private void parseArguments(String[] args) {
	boolean gotSite=false;
	boolean gotFile=false;
	boolean gotPort=false;
	boolean gotProvider=false;
	boolean gotFrequency=false;
	boolean gotUsers=false;
	boolean gotLogOutFile=false;
	int i=0;

	if (args.length !=0 && args[0].equals("--help")) {
	    printUsage();
	    System.exit(0);
	}

	while (i<args.length) {
	    if (args[i].equals("-n")) { //don't really submit anything
		reallySubmitAnything = false;
	    }
	    else if (args[i].equals("-c")) { //don't rescale graph
		constantScale = true;
	    }
	    else if (args[i].equals("-h")) { //look for host list file or host URL
		i++;
		if (loadHostListFile(args[i]))
		    gotFile=true;
		else {
		    hostName = args[i];
		    gotSite=true;
		}
	    }
	    else if (args[i].equals("-f")) { //look for frequency function
		i++;
		freqFuncName=args[i];
		gotFrequency = true;
	    }
	    else if (args[i].equals("-u")) { //look for number-of-users function
		i++;
		if (useUsersFunction(args[i])) {
		    gotUsers=true;
		} else {
		    System.out.println("There is no users function "+args[i]);
		    System.exit(-1);
		}
	    }
	    else if (args[i].equals("-p")) { //look for port
		i++;
	        port = Integer.parseInt(args[i]);
		gotPort = true;
	    }
	    else if (args[i].equals("-P")) { //look for provider
		i++;
	        provider = args[i];
		gotProvider = true;
	    }
	    else if (args[i].equals("-L")) { //look for filename for output log
		i++;
		openLogFileForWriting(args[i]);
		gotLogOutFile = true;
	    }
	    else {
		System.err.println(args[i]+" is not a recognized flag.  Use --help for more info.");
		System.exit(-1);
	    }
	    i++;
	}
	
	//For options not specified, get defaults:
	if (!gotLogOutFile) {
	    openLogFileForWriting(DEFAULT_LOGFILE);
	}
	if (!gotSite && !gotFile) {
	    if (loadHostListFile(DEFAULT_HOSTFILE))
		gotFile = true;
	    else  {
		System.err.println("Couldn't open default hostlist file hostlist.txt");
		System.exit(-1);
	    }
	}
	/*Note: we'll want to get hostname, port, and protocol seperately, if
	 * no hostfile is specified.  Default port is 8080 and default
	 * protocol is gt2.*/
	if (!gotFrequency) {
	    freqFuncName = DEFAULT_FREQUENCY;
	}

	if (!gotUsers) {
	    useUsersFunction(DEFAULT_USERS);
	}
        //Default port and protocol will be used only if a single site was given
	if (gotSite) {
	    if (!gotPort) {
		port = DEFAULT_PORT;
	    }
	    if (!gotProvider) {
		provider = DEFAULT_PROVIDER;
	    }
	    //build string to pass to JobSubmissionThread:
	    hostNameList = new String[1];
	    hostNameList[0] = new String(hostName + '\t'
					 + port + '\t' + provider);
	}
    }

    void createWindow() {
	if (constantScale)
	    theWindow = new GraphResultsFrame("Remote Grid Stress Test",
					      hostNameList,
					      new Date().getTime(),
					      DEFAULT_RANGE);
	else
	    theWindow = new GraphResultsFrame("Remote Grid Stress Test",
					      hostNameList);
    }

    void readLogFile(String filename) throws IOException {
	Date when;
	int statusCode;
	String inputLine, returningHostName;
	String[] stringArray;

	try {
	    logInput = new BufferedReader(new FileReader(filename));
	} catch (FileNotFoundException e) {
	    System.err.println("There is no file "+filename);
	    System.exit(-1);
	}

	//First line of logfile contains list of hostnames.  Read them in first.
	inputLine = logInput.readLine();
	if (inputLine == null) {
	    System.err.println("File "+filename+" is empty.");
	    System.exit(-1);
	}
	hostNameList = inputLine.split(" ");

	//Now set up window with one subgraph for each of the hosts:
	createWindow();

	/*Each line of logfile contains date of event and success or failure code.
	Read it in and turn it into a data point on the appropriate graph.*/
	inputLine = logInput.readLine();
	while (inputLine != null) {

	    stringArray = inputLine.split("\t");
	    if (stringArray.length != 3) {
		System.err.println("Bad line in logfile: "+inputLine);
		continue;
	    }
	    when = new Date(stringArray[0]);
	    returningHostName = stringArray[1];
	    statusCode = Integer.parseInt(stringArray[2]);
	    plotSuccessOrFailure(when, returningHostName, statusCode); 

	    inputLine = logInput.readLine();
	}

	logInput.close();
    }

    void openLogFileForWriting(String logFileName) {
	try {
	    File logFile = new File(logFileName);
	    if (logFile.exists() == false)
		logFile.createNewFile();
	    logWriter = new FileWriter(logFile);

	} catch (Exception e) {
	    System.err.println("Can't open file " + logFileName);
	    System.err.println(e.toString());
	    //don't quit!  proceed without logfile
	    logWriter = null;
	}
    }

    void writeLogFileHeader() {
	/*If we have a valid logFile open, start by writing the list of hosts into it
	before we start writing down success and failure events.*/
	if (logWriter != null) {
	    int i;
	    String[] tempArray;
	    try {
		for (i=0; i<hostNameList.length; i++) {
		    tempArray = hostNameList[i].split("\t");
		    logWriter.write(tempArray[0]+" ");
		}
		logWriter.write("\n");
	    }
	    catch (IOException e) {
		System.err.println("Couldn't write logfile: "+e);
	    }
	}
    }


	/*  Main thread starts one thread for each user, to the number of users given by the userFunction.  Each thread submits job, then sleeps for a time dependent on its frequencyFunction, then submits another job.  Main thread keeps running to control the other threads.  Each thread registers the main program as a listener, so the method statusChanged will get called whenever one of the remote jobs returns success or failure code.*/
    void startThreads() {
	JobSubmissionThread oneThread;
	int numberOfUsers;

	/*A few chores to take care of before starting the threads:*/
	createWindow();
	writeLogFileHeader();
 
	while (true) {
	    numberOfUsers = userFunc.getNumber();
	   
	    /*If userFunction returns a number higher than the number of threads
	      already running, start more threads.  If it returns lower, sleep some
	      of the currently running threads.*/
	    while (numberOfUsers > runningThreadList.size()) {
		if (sleepingThreadList.size() > 0) {
		    oneThread = (JobSubmissionThread)sleepingThreadList.removeLast();
		    oneThread.restart();
		}
		else {
	    /*Note that even though the same frequencyFunction subclass is passed
	      to every new thread, each thread gets a unique instance, so that they will
	      not interfere with each other's timekeeping.*/
		    oneThread = new JobSubmissionThread(this,
						makeFreqFunc(freqFuncName),
						hostNameList);
		    oneThread.start();
		    runningThreadList.addLast(oneThread);
		}
	    }
	    while (numberOfUsers < runningThreadList.size()) {
		oneThread = (JobSubmissionThread)runningThreadList.removeLast();
		oneThread.pleaseStop();
		sleepingThreadList.addLast(oneThread);
	    }
	    try {
		Thread.sleep(USER_WAIT_TIME);
		/*Wait a while, then check user function again.*/
	    }
	    catch (Exception e) {}
	}
    }

    /*this is the method to implement StatusListener.  The jobSubmissionThread registers
    StressTest as a StatusListener, so we will get a StatusEvent when the remote job 
    returns successful or not.*/
    public void statusChanged(StatusEvent event) {

	ExecutableObject eo = event.getSource(); //turn it into a hostIndex somehow
	Task returnedTask;
	String returningHostName;
	Status returnStatus = event.getStatus();
	int hostIndex; //which host = which graph does this data point go to
	Date when = new Date();

	if (eo instanceof Task) {
	    returnedTask = (Task)eo;
	    returningHostName = returnedTask.getService(0).getServiceContact().getHost();
	    
	    System.out.println("Got back message from "+returningHostName+" with status "+
			       returnStatus.getStatusCode());

	    plotSuccessOrFailure(new Date(), returningHostName, 
				 returnStatus.getStatusCode()); 
     
	    //Besides plotting this on the graph, also log it to file:
	    if (logWriter != null) {
		try {
		    String logOutput;
		    logOutput = new String(when.toString() + "\t" + returningHostName + 
					   "\t"  + returnStatus.getStatusCode() + "\n");
		    logWriter.write(logOutput, 0, logOutput.length());
		    logWriter.flush();
		} catch (IOException e) {
		    System.out.println("Couldn't write logfile: "+e);
		}
	    }
	}
    }

    private void plotSuccessOrFailure(Date when, String returningHostName, int code) {

	int hostIndex;
	//figure out which graph to add this event to, by comparing host names.
	hostIndex = 0;
	while (hostIndex < hostNameList.length) {
	    if (hostNameList[hostIndex].startsWith(returningHostName))
		break;
	    hostIndex++;
	}

	if (hostIndex >= hostNameList.length) {
	    //If this ever happens, something truly weird is going on.
	    System.err.println("I don't recognize this host name.");
	    return;
	}

	//Plot a 1 for successful job, 0 for failed job:
	if (code == Status.COMPLETED)
	    theWindow.addDataPoint(hostIndex, when, 1);
	else if (code == Status.FAILED)
	    theWindow.addDataPoint(hostIndex, when, 0);
	/*There are other statusCodes besides COMPLETED and FAILED,
	  but do not plot them.*/
    }

    public boolean isRealSubmissionMode() {
	return reallySubmitAnything;
    }

    public static void main(String[] args) {
	StressTest me = new StressTest();

	if (args.length == 2 && args[0].equals("--load")) {
	    /*stress-test --load <logfile>
	      means read in the logfile, make it into a graph, just show it,
	      don't start any submission threads.*/
	    try {
		me.readLogFile(args[1]);
	    }
	    catch (IOException e) {
		System.err.println("Could not read log file "
				   +args[1]);
		System.err.println(e);
	    }
	   
	}
	else {
	    /*The default case -- use arguments to determine host or hosts,
	      and start some threads to submit test jobs:*/
	    me.parseArguments(args);
	    if (me.logInput == null)
		me.startThreads();
	}
    }
}

