
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------



package org.apache.jetspeed.modules.actions.org.globus.cog.portlet.taskSetUpload;

// taskSetUpload imports

// cog-portlet imports
import org.globus.cog.portlet.REUtil;
import org.globus.cog.portlet.TaskSetManager;

// Castor imports
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

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
import org.globus.cog.core.interfaces.Specification;
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
import java.io.InputStreamReader;
import java.io.Reader;
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
public class TaskSetUploadPortlet extends VelocityPortletAction {

	// setup logging
	static Logger logger = Logger.getLogger(TaskSetUploadPortlet.class.getName());
 
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
			 */
		}

		// get the context path
		// this is required for accessing the help files
		strContextPath = objRunData.getContextPath();
		objContext.put("TaskSetUploadPortlet_ContextPath", strContextPath);

		// get the certificates
		try {
			if ((ProxyManager.getAllProxies(objRunData.getSession()) == null) || ((ProxyManager.getAllProxies(objRunData.getSession())).size() == 0)) {
				objContext.put("TaskSetUploadPortlet_ProxyDNs", null);	
				setTemplate(objRunData, "taskSetUpload-form.vm");
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

		objContext.put("TaskSetUploadPortlet_ProxyDNs", vecProxyDNs);
		setTemplate(objRunData, "taskSetUpload-form.vm");
	}

	/**
	 * upload the task set specification
	 */
	public void doUpload_taskset(RunData objRunData, Context objContext) {

		/**
		 * Things to do:
		 * 1) Upload the Task Set specification
		 * 2) Unmarshal the Task Set specification
		 * 3) Validate and create Tasks from the Task Set specification
		 * 4) Submit Tasks to TaskSetManager
		 * 5) Return status
		 *
		 */

		/* input */
		/* task set data */
		FileItem flItemTaskSetSpec = null;

		/* credential data */
		String strCertDN = objRunData.getParameters().getString("selCertDN", "").trim();

		/* output */
		/* to store the content to be returned to the user */
		StringBuffer strBufContent = new StringBuffer();

		/* working data variables */
		int iCount = 0;
		int iProvider = 0;
		boolean boolServiceContact = false;
		boolean boolTaskOK = false;
		boolean boolError = false;
		boolean boolInitForJob = false;
		String strSep = null;
		String strHash = null;
		String strUserId = null;
		String strURL = null;
		String strProtocol = null;
		String strIP = null;
		String strPort = null;
		String strExecutable = null;
		String strTaskType = null;
		String strTaskSetSpec = null;
		String strFileName = null;

		Enumeration enuProxyKeys = null;
		Enumeration enuTasks = null;
		Reader objReader = null;

		User objUser = null;

		GSSCredential objCred = null;
		GSSCredential objProxy = null;

		Task objTask = null;
		JobSpecification objJobSpec = null;
		ServiceContact objServiceContact = null;

		TaskSetManager objTaskSetManager = null;

		org.globus.cog.util.xml.TaskSet objTaskSet = null;

		/* input validation and transformation */
		strBufContent.append("<br>");

		/* task set spec is required */
		logger.debug("TaskSetUploadPortlet: TaskSetSpec");

		// get the file
		flItemTaskSetSpec = objRunData.getParameters().getFileItem("flTaskSetSpec");

		if ((flItemTaskSetSpec == null) || (flItemTaskSetSpec.getSize() == 0)) {
			logger.warn("TaskSetUploadPortlet: Either Task Set Specification file does not exist, or is empty.");
			boolError = true;
			strBufContent.append("Error: Either Task Set Specification file does not exist, or is empty.<br>");
		} else {
			if (objRunData.getRequest().getHeader(REUtil.HEADER_USER_AGENT).indexOf(REUtil.USER_AGENT_WIN) >= 0) {
				strSep = REUtil.SEP_WIN;
			} else {
				strSep = REUtil.SEP_UNIX;
			}

			strTaskSetSpec = flItemTaskSetSpec.getString();
			strFileName = flItemTaskSetSpec.getFileName().trim().substring(flItemTaskSetSpec.getFileName().trim().lastIndexOf(strSep)+1); // debug
			logger.debug("TaskSetUploadPortlet: Just file name: " + strFileName);
			logger.debug("TaskSetUploadPortlet: Task Set spec: " + strTaskSetSpec);

			System.out.println("YAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"); // debug
			String strTemp = objRunData.getParameters().getString("selCertDN", "").trim();
			System.out.println("selCertDN : " + strTemp); // debug
			FileItem flFI = objRunData.getParameters().getFileItem("flTaskSetSpec");
			System.out.println("flTaskSetSpec : " + flFI); // debug
			if (flFI != null) {
				System.out.println("fl get name : " + flFI.getFileName()); // debug
				System.out.println("fl get string : " + flFI.getString()); // debug
			}
			System.out.println("YAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"); // debug

			// try to unmarshal the task set
			try {
				// create string reader
				objReader = new InputStreamReader(flItemTaskSetSpec.getInputStream());
				// unmarshal the string
				objTaskSet = org.globus.cog.util.xml.TaskSet.unmarshalTaskSet(objReader);

				printTaskSet(objTaskSet);
			} catch (Exception excpE) {
				// Not able to marshal
				logger.debug("TaskSetUploadPortlet: Not able to unmarshal " + strFileName);
				// Tell user
				strBufContent.append("Error: Could not understand the Task Set Specification provided.<br>");
				strBufContent.append("     : Please make sure it conforms to the provided xml-schema(s).<br>");

				System.out.println(excpE.getMessage()); // debug
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
			logger.error("TaskSetUploadPortlet: exception caught while extracting credential()");
			boolError = true;
			strBufContent.append("Error: Not able to extract credential.<br>");
		} catch(GSSException excpGSSE) {
			logger.error("TaskSetUploadPortlet: exception caught while extracting credential()");
			boolError = true;
			strBufContent.append("Error: Not able to extract credential.<br>");
		} catch (NullPointerException excpNPE) {
		}

		if (boolError) {
			objContext.put("TaskSetUploadPortlet_Status", strBufContent.toString());
			try { 
				buildNormalContext(null, objContext, objRunData);
			} catch(Exception e) {}
			System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE"); // debug
			return;
		}

		// Iterate through the read Task Set
		enuTasks = objTaskSet.enumerateTask();
		while (enuTasks.hasMoreElements()) {
			// processing task number
			boolTaskOK = false;
			iCount++;

			System.out.println("Processing task number " + iCount); // debug

			// map elements
			org.globus.cog.util.xml.Task objXmlTask = (org.globus.cog.util.xml.Task) enuTasks.nextElement();
			objTask = fnMapTask(objXmlTask, objCred);

			// validate task, required are:
			// provider: done
			// credential: done
			// contact; has to be one of URL or Protocol,IP,Port
			strURL = objTask.getServiceContact().getURL();
			strProtocol = Integer.toString(objTask.getServiceContact().getProtocol());
			strIP = objTask.getServiceContact().getIP();
			strPort = objTask.getServiceContact().getPort();
			if ((strURL != null) && (!strURL.equals(""))) {
				boolServiceContact = true;
			}
			if ((strProtocol != null) && (!strProtocol.equals("")) &&
				(strIP != null) && (!strIP.equals("")) &&
				(strPort != null) && (!strPort.equals(""))) {
				boolServiceContact = true;
			}
			if (!boolServiceContact) {
				strBufContent.append("Error: Task " + iCount + ": Service Contact information is needed.<br>");
			} else {
				boolTaskOK = true;
			}
			// task type specific
			switch (objTask.getType()) {
				case Task.JOB_SUBMISSION:
					// executable
					strExecutable = ((JobSpecification) objTask.getSpecification()).getExecutable();
					if ((strExecutable == null) || (strExecutable.equals(""))) {
						strBufContent.append("Error: Task " + iCount + ": Executable must be specified.<br>");
					} else {
						boolTaskOK = true;
					}
					break;
				case Task.FILE_TRANSFER:
					// TODO
					break;
				case Task.INFORMATION_QUERY:
					// TODO
					break;
				default:
					// TODO: not supported, report this
			}

			// get the user id
			strUserId = ((JetspeedRunData)objRunData).getJetspeedUser().getUserId();

			// add Tasks to the TaskSet
			if (boolTaskOK) {

				System.out.println("*********** TASK " + iCount + " OK *************"); // debug
				System.out.println("Before add of " + iCount); // debug

				// get the instance of the task set manager
				objTaskSetManager = TaskSetManager.getInstance(strUserId);

				// try to add the task to the set
				try {
					objTaskSetManager.addTask(objTask);

					// Inform user
					strBufContent.append("Info: Task " + iCount + ": Added to the user task set.<br>");
					strBufContent.append("Info: Please use the User Task Set Management portlet to manage your tasks.<br>");

				} catch (Exception excpE) {
					strBufContent.append("Error: Task " + iCount + ": Could not add to user task set.<br>");
				}
			}
		}

		/* send output to view */
		strBufContent.append("<br>");
		objContext.put("TaskSetUploadPortlet_Status", strBufContent.toString());

		try { 
			buildNormalContext(null, objContext, objRunData);
		} catch(Exception e) {
		}
	}

/* debug */
	public static void printTaskSet(org.globus.cog.util.xml.TaskSet ts) {
		Enumeration enuT = ts.enumerateTask();
		while (enuT.hasMoreElements()) {
			printTask((org.globus.cog.util.xml.Task)enuT.nextElement());
		}
	}

	public static void printTask(org.globus.cog.util.xml.Task task) {
		try {
		System.out.println("Task: " + task);
		System.out.println("Name: " + task.getName());
		System.out.println("Type: " + task.getType());
		System.out.println("Provider: " + task.getProvider());
		System.out.println("Executable: " + task.getSpecification().getJobSpecification().getExecutable());
		System.out.println("Directory: " + task.getSpecification().getJobSpecification().getDirectory());
		System.out.println("Batch Job: " + task.getSpecification().getJobSpecification().getBatchJob());
		System.out.println("Service Contact: " + task.getServiceContact().getURL());
		System.out.println("Attribute count: " + task.getSpecification().getJobSpecification().getAttributeCount());
		System.out.println("Attributes: " + task.getSpecification().getJobSpecification().getAttribute(0).getName() + "=" + task.getSpecification().getJobSpecification().getAttribute(0).getValue());
		} catch (Exception excpE) {
			System.out.println("1OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO"); // debug
			System.out.println(excpE.getMessage()); // debug
			System.out.println("2OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO"); // debug
		}
	}
/* debug */

	public Task fnMapTask(org.globus.cog.util.xml.Task objXmlTask, GSSCredential objCred) {

		/* a task has the following:
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
		 *    i) attributes
		 *    then add this specification to the task
		 *
		 * 3) Create a new ServiceContact and set its parameters, like:
		 *    a) protocol
		 *    b) ip
		 *    c) port
		 *    d) url
		 *    then add this service contact to the task
		 *
		 * 4) Create a new SecurityContext and set its parameters, like:
		 *    a) credential
		 *    b) security options: delegation, xmlsecurity, authorization
		 *    c) attributes
		 *    then add this security context to the task
		**/

		int iTaskType = 0;
		int iProvider = 0;
		int iProtocol = 0;
		String strTaskName = null;
		String strTashType = null;
		String strProvider = null;
		String strCredential = null;
		String strTaskType = null;
		Enumeration enuAttr = null;

		Task objTask = null;
		Specification objTaskSpec = null;
		ServiceContact objServiceContact = null;

		org.globus.cog.util.xml.Attribute objAttr = null;

		/* if task name is not specified generate one */
		strTaskName = objXmlTask.getName();
		if ((strTaskName == null) || (strTaskName.equals(""))) {
			strTaskName = "Task_" + System.currentTimeMillis();
		}
		/* convert task type */
		strTaskType = objXmlTask.getType();
		if ((strTaskType == null) || (strTaskType.equals(""))) {
			iTaskType = 0;
		} else {
			if (strTaskType.equalsIgnoreCase("JOB_SUBMISSION")) {
				iTaskType = Task.JOB_SUBMISSION;
			} else if (strTaskType.equalsIgnoreCase("FILE_TRANSFER")) {
				iTaskType = Task.FILE_TRANSFER;
			} else if (strTaskType.equalsIgnoreCase("INFORMATION_QUERY")) {
				iTaskType = Task.INFORMATION_QUERY;
			} else {
				try {
					iTaskType = Integer.parseInt(strTaskType);
				} catch (NumberFormatException excpNFE) {
					iTaskType = 0;
				}
			}
		}
		/* provider */
		strProvider = objXmlTask.getProvider();
		if ((strProvider == null) || (strProvider.equals(""))) {
			// TODO
		} else {
			if (strProvider.equalsIgnoreCase(REUtil.JOB_NAME_GT2)) {
				iProvider = REUtil.JOB_TYPE_GT2;
			} else if (strProvider.equalsIgnoreCase(REUtil.JOB_NAME_GT3)) {
				iProvider = REUtil.JOB_TYPE_GT3;
			}
		}
		/* credential */
		strCredential = objXmlTask.getSecurityContext().getCredential();
		if ((strCredential == null) || (strCredential.equals(""))) {
			// nothing specified for credential
		} else {
			// something specified
		}

		/* task */
		objTask = new TaskImpl(strTaskName, iTaskType);
		objTask.setProvider(strProvider);

		/* specification */
		switch (objTask.getType()) {
			case Task.JOB_SUBMISSION:
				objTaskSpec = new JobSpecificationImpl();

				objTaskSpec.setType(objTask.getType());

/* commented by shankar
 * setting specification means that the rsl is specified here,
 * the handler will not try to create one!
 *
				if (objXmlTask.getSpecification().getDetail() != null) {
					objTaskSpec.setSpecification(objXmlTask.getSpecification().getDetail());
				}
*/

				if (objXmlTask.getSpecification().getJobSpecification().getExecutable() != null) {
					((JobSpecification) objTaskSpec).setExecutable(objXmlTask.getSpecification().getJobSpecification().getExecutable());
				}
				if (objXmlTask.getSpecification().getJobSpecification().getDirectory() != null) {
					((JobSpecification) objTaskSpec).setDirectory(objXmlTask.getSpecification().getJobSpecification().getDirectory());
				}
				if (objXmlTask.getSpecification().getJobSpecification().getArguments() != null) {
					((JobSpecification) objTaskSpec).setArguments(objXmlTask.getSpecification().getJobSpecification().getArguments());
				}
				if (objXmlTask.getSpecification().getJobSpecification().getStdInput() != null) {
					((JobSpecification) objTaskSpec).setStdInput(objXmlTask.getSpecification().getJobSpecification().getStdInput());
				}
				if (objXmlTask.getSpecification().getJobSpecification().getStdOutput() != null) {
					((JobSpecification) objTaskSpec).setStdOutput(objXmlTask.getSpecification().getJobSpecification().getStdOutput());
				}
				if (objXmlTask.getSpecification().getJobSpecification().getStdError() != null) {
					((JobSpecification) objTaskSpec).setStdError(objXmlTask.getSpecification().getJobSpecification().getStdError());
				}
				if (objXmlTask.getSpecification().getJobSpecification().hasBatchJob()) {
					((JobSpecification) objTaskSpec).setBatchJob(objXmlTask.getSpecification().getJobSpecification().getBatchJob());
				}
				if (objXmlTask.getSpecification().getJobSpecification().hasRedirected()) {
					((JobSpecification) objTaskSpec).setRedirected(objXmlTask.getSpecification().getJobSpecification().getRedirected());
				}
				if (objXmlTask.getSpecification().getJobSpecification().hasLocalExecutable()) {
					((JobSpecification) objTaskSpec).setLocalExecutable(objXmlTask.getSpecification().getJobSpecification().getLocalExecutable());
				}

				enuAttr = objXmlTask.getSpecification().getJobSpecification().enumerateAttribute();
				while (enuAttr.hasMoreElements()) {
					objAttr = (org.globus.cog.util.xml.Attribute) enuAttr.nextElement();
					((JobSpecification) objTaskSpec).setAttribute(objAttr.getName(), objAttr.getValue());
				}
				break;
			case Task.FILE_TRANSFER:
				// TODO
				break;
			case Task.INFORMATION_QUERY:
				// TODO
				break;
			default:
				// TODO
		}
		objTask.setSpecification(objTaskSpec);

		/* service contact */
		objServiceContact = new ServiceContactImpl();
		if (objXmlTask.getServiceContact().getURL() != null) {
			objServiceContact.setURL(objXmlTask.getServiceContact().getURL());
		}
		if (objXmlTask.getServiceContact().getProtocol() != null) {
			try {
				iProtocol = Integer.parseInt(objXmlTask.getServiceContact().getProtocol());
			} catch (NumberFormatException excpNFE) {
				iProtocol = 0;
			}
			objServiceContact.setProtocol(iProtocol);
		}
		if (objXmlTask.getServiceContact().getIP() != null) {
			objServiceContact.setIP(objXmlTask.getServiceContact().getIP());
		}
		if (objXmlTask.getServiceContact().getPort() != null) {
			objServiceContact.setPort(objXmlTask.getServiceContact().getPort());
		}
		objTask.setServiceContact(objServiceContact);

		/* security context */
		if (iProvider == REUtil.JOB_TYPE_GT2) {
			org.globus.cog.core.impl.gt2.GlobusSecurityContextImpl objSecurityContext =
				new org.globus.cog.core.impl.gt2.GlobusSecurityContextImpl();

			objSecurityContext.setCredentials(objCred);
			enuAttr = objXmlTask.getSecurityContext().enumerateAttribute();
			while (enuAttr.hasMoreElements()) {
				objAttr = (org.globus.cog.util.xml.Attribute) enuAttr.nextElement();
				objSecurityContext.setAttribute(objAttr.getName(), objAttr.getValue());
			}
			objTask.setSecurityContext(objSecurityContext);
		} else if (iProvider == REUtil.JOB_TYPE_GT3) {
			org.globus.cog.core.impl.gt3.GlobusSecurityContextImpl objSecurityContext =
				new org.globus.cog.core.impl.gt3.GlobusSecurityContextImpl();

			objSecurityContext.setCredentials(objCred);
/* commented by shankar
 * TODO
 * check for null values
 * and then create and assign appropriate objects
			objSecurityContext.setAuthorization(objXmlTask.getSecurityContext().getAuthorization());
			objSecurityContext.setXMLSec(objXmlTask.getSecurityContext().getXmlSec());
			objSecurityContext.setDelegation(objXmlTask.getSecurityContext().getDelegation());
*/
			enuAttr = objXmlTask.getSecurityContext().enumerateAttribute();
			while (enuAttr.hasMoreElements()) {
				objAttr = (org.globus.cog.util.xml.Attribute) enuAttr.nextElement();
				objSecurityContext.setAttribute(objAttr.getName(), objAttr.getValue());
			}
			objTask.setSecurityContext(objSecurityContext);
		}

		return objTask;
	}

} /* end clsVelocityPortletAction */

