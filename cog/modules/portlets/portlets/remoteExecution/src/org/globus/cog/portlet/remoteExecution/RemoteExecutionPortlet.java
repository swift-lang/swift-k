
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------



package org.apache.jetspeed.modules.actions.org.globus.cog.portlet.remoteExecution;

// remoteExecution imports
import org.globus.cog.portlet.remoteExecution.JobInfo;

// cog-portlet imports
import org.globus.cog.portlet.REUtil;
import org.globus.cog.portlet.TaskSetManager;

// Apache imports
import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
//import org.apache.jetspeed.om.security.JetspeedUser;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.jetspeed.services.JetspeedSecurity;
import org.apache.jetspeed.services.rundata.JetspeedRunData;
import org.apache.jetspeed.services.security.JetspeedSecurityException;
import org.apache.log4j.Logger;
import org.apache.turbine.om.security.User;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.upload.FileItem;
import org.apache.velocity.context.Context;

// Extreme proxymanager imports
import xportlets.proxymanager.ProxyManager;
import xportlets.proxymanager.ProxyStorageException;

// Cog core imports
import org.globus.cog.core.impl.common.IllegalSpecException;
import org.globus.cog.core.impl.common.InvalidSecurityContextException;
import org.globus.cog.core.impl.common.InvalidServiceContactException;
import org.globus.cog.core.impl.common.TaskSubmissionException;
import org.globus.cog.core.impl.common.GenericTaskHandler;
import org.globus.cog.core.impl.common.JobSpecificationImpl;
import org.globus.cog.core.impl.common.ServiceContactImpl;
import org.globus.cog.core.impl.common.TaskImpl;
import org.globus.cog.core.interfaces.JobSpecification;
import org.globus.cog.core.interfaces.ServiceContact;
import org.globus.cog.core.interfaces.Status;
import org.globus.cog.core.interfaces.Task;
import org.globus.cog.core.interfaces.TaskHandler;

// Cog imports
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;

// IETF imports
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

// Java imports
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * remember:
 * - check all objects for null before refrencing them
 * - check all strings for null and "" before doing anything with them
 * - use interfaces like Lists and not implementation like Vectors
 *
 * todo:
 *
 *
 */
public class RemoteExecutionPortlet extends VelocityPortletAction {

	// setup logging
	static Logger logger = Logger.getLogger(RemoteExecutionPortlet.class.getName());

	public void buildNormalContext( VelocityPortlet objPortlet, Context objContext, RunData objRunData )
		throws Exception {

		String strHash = null;
		String strInitDir = null;
		String strContextPath;
		Vector vecProxyDNs = null;
		Enumeration enuProxyKeys = null;
		User objUser = null;
		GSSCredential objProxy = null;

		// to initialize the REUtil
		try {
			strInitDir = objRunData.getServletContext().getRealPath(REUtil.getPathSep()).trim();
			REUtil.init(strInitDir);
		} catch (Exception excpE) {
			/**
			 * todo:
			 * a) disable portlet?, or
			 * b) reduce functionality
			 * c) inform user
			 */
		}

		// get the context path
		// this is required for accessing the help files
		strContextPath = objRunData.getContextPath();
		objContext.put("RemoteExecutionPortlet_ContextPath", strContextPath);

		// get the certificates
		try {
			if ((ProxyManager.getAllProxies(objRunData.getSession()) == null) || ((ProxyManager.getAllProxies(objRunData.getSession())).size() == 0)) {
				objContext.put("RemoteExecutionPortlet_ProxyDNs", null);	
				setTemplate(objRunData, "remoteExecution-form.vm");
				return;
			}
			vecProxyDNs = new Vector();
			enuProxyKeys = ProxyManager.getAllProxies(objRunData.getSession()).keys();
			while (enuProxyKeys.hasMoreElements()) {
				strHash = (String) enuProxyKeys.nextElement();
				objProxy = ProxyManager.getProxy(objRunData.getSession(), strHash);
				if ((objProxy instanceof GlobusGSSCredentialImpl) && (objProxy.getRemainingLifetime() > 0)) {
					vecProxyDNs.add(objProxy.getName().toString());
				}
			}
		} catch (ProxyStorageException excpPSE) {
		} catch (NullPointerException excpNPE) {
		}

		objContext.put("RemoteExecutionPortlet_ProxyDNs", vecProxyDNs);
		setTemplate(objRunData, "remoteExecution-form.vm");
	}

	/**
	 * submit the job to run
	 */
	public void doSubmit_job(RunData objRunData, Context objContext) {

		/**
		 * Things to do:
		 * 1) Create a new Task and set its parameters, like:
		 *    a) name
		 *    b) type
		 *
		 * 2) Create a new JobSpecification and set its parameters, like:
		 *    a) executable
		 *    b) arguments
		 *    c) directory
		 *    d) environment
		 *    e) stdin
		 *    f) stdout
		 *    g) stderr
		 *    h) batchJob
		 *    then add this specification to the task
		 *
		 * 3) Create a new SecurityContext and set its parameters, like:
		 *    a) credential
		 *    then add this security context to the task
		 *
		 * 4) Create a new ServiceContact and set its parameters, like:
		 *    a) protocol
		 *    b) ip
		 *    c) port
		 *    d) url
		 *    then add this service contact to the task
		 *
		 * 5) Create a new TaskHandler, of the specific type chosen, and
		 *    submit the job for execution.
		 *
		 */

		/* input */
		/* task data */
		String strJobName = objRunData.getParameters().getString("txtJobName", "").trim();
		String strProvider = objRunData.getParameters().getString("selProvider", "").trim();

		/* service contact data */
		String strHostContact = objRunData.getParameters().getString("txtHostContact", "").trim();
		String strPort = objRunData.getParameters().getString("txtPort", "").trim();
		String strService = objRunData.getParameters().getString("txtService", "").trim();
		String strSubject = objRunData.getParameters().getString("txtSubject", "").trim();

		/* credential data */
		String strCertDN = objRunData.getParameters().getString("selCertDN", "").trim();

		/* job specification data */
		/* executable */
		String strExecutable = null;
		FileItem flItemExecutable = null;
		String strExecutableType = objRunData.getParameters().getString("txtExecutableType", "").trim();
		String strArguments = objRunData.getParameters().getString("txtArguments", "").trim();
		String strDirectory = objRunData.getParameters().getString("txtDirectory", "").trim();
		String strEnvironment = objRunData.getParameters().getString("txtEnvironment", "").trim();
		/* stdin */ // todo
					// objRunData.getParameters().getString("txtStdin", "").trim(); // todo
		String strStdInput = "";
		String strStdOutput = objRunData.getParameters().getString("txtStdOut", "").trim();
		String strStdError = objRunData.getParameters().getString("txtStdErr", "").trim();
		String strRedirected = objRunData.getParameters().getString("selRedirected", "").trim();
		String strBatchJob = objRunData.getParameters().getString("selBatchJob", "").trim();

		/* output */
		/* to store the content to be returned to the user */
		StringBuffer strBufContent = new StringBuffer();

		/* working data variables */
		int intProvider = 0;
		boolean boolBatchJob = false;
		boolean boolRedirected = false;
		boolean boolError = false;
		boolean boolInitForJob = false;
		boolean boolLocalExecutable = false;
		String strHash = null;
		String strServiceContact = null;
		String strFileName = null;
		String strUserId = null;
		Vector vecHosts = null;
		Vector vecJobHistory = null;

		Enumeration enuProxyKeys = null;

		User objUser = null;

		GSSCredential objCred = null;
		GSSCredential objProxy = null;

		Task objTask = null;
		JobSpecification objJobSpec = null;
		ServiceContact objServiceContact = null;
		TaskHandler objTaskHandler = null;

		TaskSetManager objTaskSetManager = null;

		/* input validation and transformation */
		strBufContent.append("<br>");
		/* if job name is not specified generate one */
		if ((strJobName == null) || (strJobName.equals(""))) {
			strJobName = "Job_" + System.currentTimeMillis();
			strBufContent.append("Info: Job name set to \'" + strJobName + "\'.<br>");
		}
		/* create Task */
		objTask = new TaskImpl(strJobName, Task.JOB_SUBMISSION);

		/* provider is required */
		if ((strProvider == null) || (strProvider.equals(""))) {
			boolError = true;
			strBufContent.append("Error: Provider is required.<br>");
		} else if (!(strProvider.equalsIgnoreCase(REUtil.JOB_NAME_GT2) || strProvider.equalsIgnoreCase(REUtil.JOB_NAME_GT3))) {
			boolError = true;
			strBufContent.append("Error: Provider should be, one of, \'" + REUtil.JOB_NAME_GT2 + "\' or \'" + REUtil.JOB_NAME_GT3 + "\'.<br>");
		} else {
			if (strProvider.equalsIgnoreCase(REUtil.JOB_NAME_GT2)) {
				intProvider = REUtil.JOB_TYPE_GT2;
			} else if (strProvider.equalsIgnoreCase(REUtil.JOB_NAME_GT3)) {
				intProvider = REUtil.JOB_TYPE_GT3;
			}
		}

		/* hostcontact is required */
		if ((strHostContact == null) || (strHostContact.equals(""))) {
			boolError = true;
			strBufContent.append("Error: Hostname is required.<br>");
		}
		/* executable is required */
		if ((strExecutableType == null) || (strExecutableType.equals(""))) {
			boolError = true;
			strBufContent.append("Error: Executable is required.<br>");
		} else {

			logger.debug("RemoteExecutionPortlet: strExecutableType : \'" + strExecutableType + "\'");

			// check executable type and accordingly report
			if (strExecutableType.toLowerCase().equalsIgnoreCase("remote")) {
				// check for string
				strExecutable = objRunData.getParameters().getString("txtExecutable", "").trim();
				if ((strExecutable == null) || (strExecutable.equals(""))) {
					boolError = true;
					strBufContent.append("Error: Executable is required.<br>");
				}
			} else if (strExecutableType.toLowerCase().equalsIgnoreCase("local")) {

				logger.debug("RemoteExecutionPortlet: Local");

				// get the file
				flItemExecutable = objRunData.getParameters().getFileItem("flExecutable");

				String strSep = null;
				if (objRunData.getRequest().getHeader(REUtil.HEADER_USER_AGENT).indexOf(REUtil.USER_AGENT_WIN) >= 0) {
					strSep = REUtil.SEP_WIN;
				} else {
					strSep = REUtil.SEP_UNIX;
				}

				strFileName = flItemExecutable.getFileName().trim().substring(flItemExecutable.getFileName().trim().lastIndexOf(strSep)+1);

				logger.debug("RemoteExecutionPortlet: Just file name: " + strFileName);

				boolInitForJob = false;
				try {
					REUtil.initForTask(Long.toString(objTask.getIdentity().getValue()));
					boolInitForJob = true;
				} catch (Exception excpE) {
					logger.error("RemoteExecutionPortlet: exception caught while doing REUtil.initForTask()");
				}
				if (boolInitForJob) {
					try {
						strExecutable = REUtil.getPathSep() + REUtil.getREPath() + REUtil.getPathSep() + REUtil.getTaskDir(objTask.getIdentity().getValue()) + REUtil.getPathSep() + strFileName;
						flItemExecutable.write(strExecutable);
						boolLocalExecutable = true;
					} catch (Exception excpE) {
						// could not upload job
						boolError = true;
						strBufContent.append("Error: Could not upload executable.<br>");
					}
				} else {
					// could not upload job
					boolError = true;
					strBufContent.append("Error: Could not upload executable.<br>");
				}
			} else {
				// no executable type specified
				boolError = true;
				strBufContent.append("Error: Executable is required.<br>");
			}
		}
		/* redirected input/output or not? */
		if ((strRedirected == null) || (strRedirected.equals("")) || (strRedirected.equalsIgnoreCase("false"))) {
			boolRedirected = false;
		} else {

			logger.debug("RemoteExecutionPortlet: Is redirected");

			// set up the standard output and error
			strStdOutput = "";
			strStdError = "";
			boolRedirected = true;

		}
		/* batch job or not? */
		if ((strBatchJob == null) || (strBatchJob.equals("")) || (strBatchJob.equalsIgnoreCase("false"))) {
			boolBatchJob = false;
		} else {
			if (boolLocalExecutable) {
				// this is not allowed
				boolError = true;
				strBufContent.append("Error: Currently local executables cant be executed in Batch mode.<br>");
			} else if (boolRedirected) {
				// this is not allowed
				strBufContent.append("Warn: Both Batch and Redirected can't be chosen. Defaulting to Batch mode.<br>");
			} else {
				boolBatchJob = true;
				strBufContent.append("Info: Running job in batch mode.<br>");
			}
		}
		/* check for valid credential */
		/* Get the proxy credential, selected by the user
		 * This is a matching process right now.
		 * We take the Cert. DN selected by the user,
		 * and compare it with the DNs of all the certificates
		 * currently with the user.
		 * When a match is found, that's the proxy
		 *
		 * Changes for this to work with ver 2.0 are:
		 * - Use the ProxyManager to access the proxies
		 * - Use the getDefaultProxy() and the getAllProxies() 
		 *   methods to retreive the proxies
		 */
		objCred = null;
		try {
			if (ProxyManager.getAllProxies(objRunData.getSession()) != null) {
				enuProxyKeys = ProxyManager.getAllProxies(objRunData.getSession()).keys();
				while (enuProxyKeys.hasMoreElements()) {
					strHash = (String) enuProxyKeys.nextElement();
					objProxy = ProxyManager.getProxy(objRunData.getSession(), strHash);
					if ((objProxy instanceof GlobusGSSCredentialImpl) && (objProxy.getName().toString().equals(strCertDN))) {
						objCred = objProxy;
						break;
					}
				}
				if (objCred == null) {
					boolError = true;
					strBufContent.append("Error: No valid credential was found. It may have expired!<br>");
				}
			}
		} catch (ProxyStorageException excpPSE) {
			logger.error("RemoteExecutionPortlet: exception caught while extracting credential()");
			boolError = true;
			strBufContent.append("Error: Not able to extract credential.<br>");
		} catch(GSSException excpGSSE) {
			logger.error("RemoteExecutionPortlet: exception caught while extracting credential()");
			boolError = true;
			strBufContent.append("Error: Not able to extract credential.<br>");
		} catch (NullPointerException excpNPE) {
		}

		if (boolError) {
			objContext.put("RemoteExecutionPortlet_Status", strBufContent.toString());
			try { 
				buildNormalContext(null, objContext, objRunData);
			} catch(Exception e) {}
			setTemplate(objRunData, "remoteExecution-form.vm");
			return;
		}

		logger.debug("RemoteExecutionPortlet: strExecutable : \'" + strExecutable + "\'");
		logger.debug("RemoteExecutionPortlet: strEnvironment = \'" + strEnvironment + "\'");

		/* setup */
		/* get the User object */
		objUser = objRunData.getUser();

		/* get the previous hostcontact strings */
		vecHosts = (Vector)(objUser.getTemp("RemoteExecutionPortlet_Hosts", new Vector()));

		/* remove duplicate if present */
		vecHosts.remove(strHostContact);
		/* add the currently chosen hostname to the top of the list */
		vecHosts.add(0, strHostContact);

		/* save the hostcontact strings */
		objUser.setTemp("RemoteExecutionPortlet_Hosts", vecHosts);
	
		/* If a Job history vector is already present in session, get it
		 * Otherwise create a new one.
		 */
		vecJobHistory = (Vector) (objRunData.getUser().getTemp("RemoteExecutionPortlet_JobHistory", new Vector()));

		/* setup the service Contact from the user input */
		switch (intProvider) {
			case REUtil.JOB_TYPE_GT2 :
				strServiceContact = fnCreateServiceContact(strHostContact, strPort, strService, strSubject);
				break;
			case REUtil.JOB_TYPE_GT3 :
				strServiceContact = strHostContact;
				break;
			default :
				strServiceContact = "";
				break;
		}

		/* create and set JobSpecification */
		objJobSpec = new JobSpecificationImpl();
		// has to be there
		objJobSpec.setExecutable(strExecutable);

		objJobSpec.setLocalExecutable(boolLocalExecutable);

		if (!strDirectory.equals("")) {
			objJobSpec.setDirectory(strDirectory);
		}
		if (!strArguments.equals("")) {
			objJobSpec.setArguments(strArguments);
		}
		if (!strEnvironment.equals("")) {
			objJobSpec.setAttribute(REUtil.ATTRIBUTE_ENVIRONMENT, strEnvironment);
		}
		if (!strStdInput.equals("")) {
			objJobSpec.setStdInput(strStdInput);
		}
		if (!strStdOutput.equals("")) {
			objJobSpec.setStdOutput(strStdOutput);
		}
		if (!strStdError.equals("")) {
			objJobSpec.setStdError(strStdError);
		}
		objJobSpec.setRedirected(boolRedirected);
		objJobSpec.setBatchJob(boolBatchJob);
		objTask.setSpecification(objJobSpec);

		/* create and set SecurityContext */ 
		if (intProvider == REUtil.JOB_TYPE_GT2) {
			org.globus.cog.core.impl.gt2.GlobusSecurityContextImpl objSecurityContext = 
				new org.globus.cog.core.impl.gt2.GlobusSecurityContextImpl();
			objSecurityContext.setCredentials(objCred);
			objTask.setSecurityContext(objSecurityContext);
		} else if (intProvider == REUtil.JOB_TYPE_GT3) {
			org.globus.cog.core.impl.gt3.GlobusSecurityContextImpl objSecurityContext = 
				new org.globus.cog.core.impl.gt3.GlobusSecurityContextImpl();
			objSecurityContext.setCredentials(objCred);
			objTask.setSecurityContext(objSecurityContext);
		}

		/* create and set ServiceContact */
		objServiceContact = new ServiceContactImpl(strServiceContact);
		objTask.setServiceContact(objServiceContact);

		/* create and add the Task */
		if (boolLocalExecutable) {
			objTask.setAttribute(REUtil.RE_EXECUTABLE_NAME, strFileName);
		}
		vecJobHistory.add(objTask);
		/* save the job history */
		objRunData.getUser().setTemp("RemoteExecutionPortlet_JobHistory", vecJobHistory);

		logger.debug("RemoteExecutionPortlet: strProvider = \'" + strProvider + "\'");

		/* set the provider */
		objTask.setProvider(strProvider);

/* commented by shankar
		// New way of sumibtting the job, instead of the generic handler
		// get an instance of the TaskSetManager, and add the job.

		// Forget about getting status for now.. just say sumitted!

		/ * create the task handler and submit the job * /
		objTaskHandler = new GenericTaskHandler();
		try {
			/ * submit the job * /
			objTaskHandler.submit(objTask);
		} catch (InvalidSecurityContextException excpISCE) {
			strBufContent.append("Error: Invalid Security Context.<br>");
		} catch (TaskSubmissionException excpTSE) {
			strBufContent.append("Error: with Task Submission.<br>");
		} catch (IllegalSpecException excpISE) {
			strBufContent.append("Error: Illegal Task specification.<br>");
		} catch (InvalidServiceContactException excpISCE) {
			strBufContent.append("Error: Invalid Service Contact.<br>");
		}

		/ * get status and display if not batch job * /
		if (!boolBatchJob) {
			/ * loop till completed or something happens, and then get output * /
			while (!(objTask.isCompleted() || objTask.isFailed() || objTask.isCanceled())) {
				try {
					Thread.sleep(500);
				} catch (Exception excpE) {
				}
			}
			if (objTask.isCompleted()) {
				strBufContent.append("Info: Job executed successfully.<br>");
				strBufContent.append("Info: Please check the Standard Output and Standard Error files for any messages.<br>");
			} else if (objTask.isFailed()) {
				strBufContent.append("Info: Job execution FAILED.<br>");
			} else if (objTask.isCanceled()) {
				strBufContent.append("Info: Job execution CANCELED.<br>");
			}

			logger.debug("RemoteExecutionPortlet: Job status: " + objTask.getStatus().getStatus());
			logger.debug("RemoteExecutionPortlet: Status message: " + objTask.getStatus().getMessage());
		}
*/
		// get the user id
		strUserId = ((JetspeedRunData)objRunData).getJetspeedUser().getUserId();

		// get the instance of the task set manager
		objTaskSetManager = TaskSetManager.getInstance(strUserId);

		// try to add the task to the set
		try {
			objTaskSetManager.addTask(objTask);

			// Inform user
			strBufContent.append("Info: Job added to the user task set.<br>");
			strBufContent.append("Info: Please use the User Task Set Management portlet to manage your tasks.<br>");

		} catch (Exception excpE) {
			strBufContent.append("Error: Could not add Job to user task set.<br>");
		}

		/* send output to view */
		strBufContent.append("<br>");
		objContext.put("RemoteExecutionPortlet_Status", strBufContent.toString());

		try { 
			buildNormalContext(null, objContext, objRunData);
		} catch(Exception e) {}
		setTemplate(objRunData, "remoteExecution-form.vm");
	}
 
	public void doShow_main_screen(RunData objRunData, Context objContext)
	{
		try {
			buildNormalContext(null, objContext, objRunData);
		} catch(Exception e) {}
		setTemplate(objRunData, "remoteExecution-form.vm");
	}
    
	public void doShow_job_history(RunData objRunData, Context objContext)
	{  
		boolean boolLocalExecutable = false;
		boolean boolRedirected = false;
		String strJobName = null;
		String strURL = null;
		String strExecutable = null;
		String strExecutableName = null;
		String strStatus = null;
		String strStdOutput = null;
		String strStdError = null;
		String strStdOutputPath = null;
		String strStdErrorPath = null;
		String strStdOutputFileName = null;
		String strStdErrorFileName = null;
		String strArguments = null;
		String strDirectory = null;
		Vector vecAllJobs = null;
		Vector vecJobHistory = null;
		Enumeration enuJobHistory = null;
		FileWriter flFileWriter = null;
		Task objTask = null;
		Status objStatus = null;
		JobInfo objJobInfo = null;

		vecAllJobs = new Vector();
		vecJobHistory = (Vector)(objRunData.getUser().getTemp("RemoteExecutionPortlet_JobHistory", new Vector()));
		objRunData.getUser().setTemp("RemoteExecutionPortlet_JobHistory", vecJobHistory);

		logger.debug("RemoteExecutionPortlet: Length of vecJobHistory: " + vecJobHistory.size());

		enuJobHistory = vecJobHistory.elements();
		while (enuJobHistory.hasMoreElements()) {
			objTask = (Task)enuJobHistory.nextElement();
			objStatus = objTask.getStatus();

			/* job name */
			strJobName = objTask.getName();
			/* url */
			strURL = objTask.getServiceContact().getURL();
			/* executable */
			boolLocalExecutable = ((JobSpecification)(objTask.getSpecification())).isLocalExecutable();
			strExecutable = ((JobSpecification)(objTask.getSpecification())).getExecutable();
			strExecutableName = (String)objTask.getAttribute(REUtil.RE_EXECUTABLE_NAME);
			/* arguments and directory */
			strArguments = ((JobSpecification)(objTask.getSpecification())).getArguments();
			strDirectory = ((JobSpecification)(objTask.getSpecification())).getDirectory();
			/* status */
			switch (objStatus.getStatus()) {
				case Status.UNSUBMITTED :
					strStatus = "Not Submitted";
					break;
				case Status.SUBMITTED :
					strStatus = "Submitted";
					break;
				case Status.ACTIVE :
					strStatus = "Active";
					break;
				case Status.SUSPENDED :
					strStatus = "Suspended";
					break;
				case Status.RESUMED :
					strStatus = "Resumed";
					break;
				case Status.FAILED :
					strStatus = "Failed";
					break;
				case Status.CANCELED :
					strStatus = "Cancelled";
					break;
				case Status.COMPLETED :
					strStatus = "Completed";
					break;
				default :
					strStatus = "Unknown";
			}
			/* std output and std error */
			boolRedirected = ((JobSpecification)(objTask.getSpecification())).isRedirected();
			if (boolRedirected) {
				strStdOutput = objTask.getStdOutput();
				strStdError = objTask.getStdError();
				if (!boolLocalExecutable) {
					try {
						REUtil.initForJob(strJobName);
					} catch (Exception excpE) {
						logger.error("RemoteExecutionPortlet: Exception caught while doing REUtil.initForJob()");
					}
				}
				try {
					strStdOutputFileName = REUtil.touchFile(strJobName, REUtil.RE_STD_OUT_FILE_NAME);
					flFileWriter = new FileWriter(strStdOutputFileName);
					flFileWriter.write(REUtil.RE_FILES_HTML_HEADER_1 + REUtil.RE_STD_OUT_HTML_TITLE + REUtil.RE_FILES_HTML_HEADER_2);
					flFileWriter.write(strStdOutput);
					flFileWriter.write(REUtil.RE_FILES_HTML_FOTTER);
					flFileWriter.flush();
					flFileWriter.close();
					strStdErrorFileName = REUtil.touchFile(strJobName, REUtil.RE_STD_ERR_FILE_NAME);
					flFileWriter = new FileWriter(strStdErrorFileName);
					flFileWriter.write(REUtil.RE_FILES_HTML_HEADER_1 + REUtil.RE_STD_ERR_HTML_TITLE + REUtil.RE_FILES_HTML_HEADER_2);
					flFileWriter.write(strStdError);
					flFileWriter.write(REUtil.RE_FILES_HTML_FOTTER);
					flFileWriter.flush();
					flFileWriter.close();
				} catch (Exception excpE) {
					logger.error("RemoteExecutionPortlet: Exception caught while doing REUtil.touchStdOutput/Error()");
				}
			}
			strStdOutputPath = REUtil.REMOTE_EXECUTION_DIR + REUtil.getPathSep() + strJobName + REUtil.getPathSep() + REUtil.RE_STD_OUT_FILE_NAME;
			strStdErrorPath = REUtil.REMOTE_EXECUTION_DIR + REUtil.getPathSep() + strJobName + REUtil.getPathSep() + REUtil.RE_STD_ERR_FILE_NAME;

			/* set into bean and add bean to vector */
			objJobInfo = new JobInfo(strJobName, 
									strURL, 
									boolLocalExecutable, strExecutable, strExecutableName, 
									strArguments, strDirectory,
									boolRedirected, strStdOutputPath, strStdErrorPath,
									strStatus);

			logger.debug("Job Name\t:" + objJobInfo.getName());
			logger.debug("URL\t\t:" + objJobInfo.getURL());
			logger.debug("LocalExecutable\t:" + objJobInfo.getLocalExecutable());
			logger.debug("Executable\t:" + objJobInfo.getExecutable());
			logger.debug("ExecutableName\t:" + objJobInfo.getExecutableName());
			logger.debug("Arguments\t:" + objJobInfo.getArguments());
			logger.debug("Directory\t:" + objJobInfo.getDirectory());
			logger.debug("Redirected\t:" + objJobInfo.getRedirected());
			logger.debug("StdOutput\t:" + objJobInfo.getStdOutput());
			logger.debug("StdError\t:" + objJobInfo.getStdError());
			logger.debug("Status\t\t:" + objJobInfo.getStatus());

			vecAllJobs.add(objJobInfo);
		}

		logger.debug("length of vecAllJobs: " + vecAllJobs.size());

		objContext.put("RemoteExecutionPortlet_AllJobs", vecAllJobs);
		try { 
			buildNormalContext(null, objContext, objRunData);
		} catch(Exception e) {}
		setTemplate(objRunData, "remoteExecution-view.vm");
	}

	public String fnCreateServiceContact(String strHostName, String strPort, String strService, String strSubject) {

		String strServiceContact = null;

        /* setup the service Contact from the user input */
        if((strPort != null) && (!strPort.equals(""))) {
            if ((strService != null) && (!strService.equals(""))) {
                if ((strSubject != null) && (!strSubject.equals(""))) {
                    /* type:
                     *
                     * host:port/service:subject
                     */
                    strServiceContact = strHostName.concat(":").concat(strPort).concat("/").concat(strService).concat(":").concat(strSubject).trim();
                } else {
                    /* type:
                     *
                     * host:port/service
                     */
                    strServiceContact = strHostName.concat(":").concat(strPort).concat("/").concat(strService).trim();
                }
            } else {
                if ((strSubject != null) && (!strSubject.equals(""))) {
                    /* type:
                     *
                     * host:port:subject
                     */
                    strServiceContact = strHostName.concat(":").concat(strPort).concat(":").concat(strSubject).trim();
                } else {
                    /* type:
                     *
                     * host:port
                     */
                    strServiceContact = strHostName.concat(":").concat(strPort).trim();
                }
            }
        } else {
            if ((strService != null) && (!strService.equals(""))) {
                if ((strSubject != null) && (!strSubject.equals(""))) {
                    /* type:
                     *
                     * host/service:subject
                     * host:/service:subject
                     */
                    strServiceContact = strHostName.concat(":").concat("/").concat(strService).concat(":").concat(strSubject).trim();
                } else {
                    /* type:
                     *
                     * host/service
                     * host:/service
                     */
                    strServiceContact = strHostName.concat(":").concat("/").concat(strService).trim();
                }
            } else {
                if ((strSubject != null) && (!strSubject.equals(""))) {
                    /* type:
                     *
                     * host::subject
                     */
                    strServiceContact = strHostName.concat(":").concat(strSubject).trim();
                } else {
                    /* type:
                     *
                     * host
                     */
                    strServiceContact = strHostName.trim();
                }
            }
        }

		return strServiceContact;
	} /* end fnCreateServiceContact */

} /* end clsVelocityPortletAction */

