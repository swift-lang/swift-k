
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------



package org.apache.jetspeed.modules.actions.org.globus.cog.portlet.setManage;

// setManage imports
//import org.globus.cog.portlet.setManage.??;

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

// Java CoG Core imports
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
import org.globus.cog.core.interfaces.SetStatus;
import org.globus.cog.core.interfaces.Status;
import org.globus.cog.core.interfaces.Task;
import org.globus.cog.core.interfaces.TaskSet;
import org.globus.cog.core.interfaces.TaskHandler;

// IETF imports
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

// Java imports
import java.io.FileWriter;
import java.util.Enumeration;
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
public class SetManagePortlet extends VelocityPortletAction {

	// setup logging
	static Logger logger = Logger.getLogger(SetManagePortlet.class.getName());

	// static
	public static final String VIEW_STATE = "view_state";
	public static final String VIEW_DETAIL = "detail";
	public static final String VIEW_MINIMAL = "minimal";
	public static final String TIME_DIFF = "time_diff";

	/**
	 * build Normal Context
	 *
	 *
	 */
	public void buildNormalContext( VelocityPortlet objPortlet, Context objContext, RunData objRunData )
		throws Exception {

		String strState = null;
		String strUserId = null;
		String strInitDir = null;
		SetStatus objSetStatus = null;
		Task[] arrayTasks = null;
		TaskSet objTaskSet = null;
		TaskSetManager objTaskSetManager = null;

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

		strUserId = ((JetspeedRunData)objRunData).getJetspeedUser().getUserId();

		// get the instance of the task set manager
		objTaskSetManager = TaskSetManager.getInstance(strUserId);

		// get the task set from the manager
		objTaskSet = objTaskSetManager.getTaskSet();

		// get the array of tasks from the task set
		arrayTasks = objTaskSetManager.getTasksArray();
		// save the stdOutput and stdError to files for easier access
		// save the total time taken
		// and provide this information through the task
		translateOutput(arrayTasks);

		// save the task set into the context
		objContext.put("SetManagePortlet_TaskSet", objTaskSet);

		// save the task array into the context
		objContext.put("SetManagePortlet_AllTasks", arrayTasks);

		strState = (String)objRunData.getSession().getAttribute(VIEW_STATE);
		if (strState == null) {
			objRunData.getSession().setAttribute(VIEW_STATE, VIEW_DETAIL);
			strState = VIEW_DETAIL;
		}
		if (strState.equalsIgnoreCase(VIEW_DETAIL)) {
			setTemplate(objRunData, "setManage-view.vm");
		}
		if (strState.equalsIgnoreCase(VIEW_MINIMAL)) {
			setTemplate(objRunData, "setManage-view-minimal.vm");
		}
	}

	public void doShow_detail(RunData objRunData, Context objContext) {
		objRunData.getSession().setAttribute(VIEW_STATE, VIEW_DETAIL);
		try { 
			buildNormalContext(null, objContext, objRunData);
		} catch(Exception e) {
		}
		return;
	}

	public void doShow_minimal(RunData objRunData, Context objContext) {
		objRunData.getSession().setAttribute(VIEW_STATE, VIEW_MINIMAL);
		try { 
			buildNormalContext(null, objContext, objRunData);
		} catch(Exception e) {
		}
		return;
	}

	public void doTask_operation(RunData objRunData, Context objContext) {
		// input
		// task data
		String strOperationType = objRunData.getParameters().getString("txtOperationType", "").trim();
		String strTaskId = objRunData.getParameters().getString("txtTaskId", "").trim();
		String strTaskName = objRunData.getParameters().getString("txtTaskName", "").trim();

		// output
		// to store the content to be returned to the user
		StringBuffer strBufContent = new StringBuffer();

		// working data variables
		boolean boolError = false;
		boolean boolOperationCompleted = false;
		String strUserId = null;
		TaskSetManager objTaskSetManager = null;

		// input validation and transformation
		strBufContent.append("<br>");
		// input validation
		if ((strOperationType == null) || (strOperationType.equals(""))) {
			boolError = true;
			strBufContent.append("Error: Operation cannot be empty.<br>");
		} else {
			if ((!strOperationType.equalsIgnoreCase("suspend")) &&
				(!strOperationType.equalsIgnoreCase("resume")) &&
				(!strOperationType.equalsIgnoreCase("cancel")) &&
				(!strOperationType.equalsIgnoreCase("delete")) &&
				(!strOperationType.equalsIgnoreCase("deleteAll"))) { 
				strBufContent.append("Error: Could not perform operation " + strOperationType + ",<br>");
				strBufContent.append("     : on task " + strTaskName + ".<br>");
				strBufContent.append("     : Operation not supported.<br>");
			}
		}
		if ((strTaskId == null) || (strTaskId.equals(""))) {
			boolError = true;
			strBufContent.append("Error: Task Id cannot be empty.<br>");
		}
		if (boolError) {
			objContext.put("SetManagePortlet_Error", "true");
			objContext.put("SetManagePortlet_Status", strBufContent.toString());
			try { 
				buildNormalContext(null, objContext, objRunData);
			} catch(Exception e) {
			}
//			setTemplate(objRunData, "setManage-view.vm");
			return;
		}

		strUserId = ((JetspeedRunData)objRunData).getJetspeedUser().getUserId();

		// get the instance of the task set manager
		objTaskSetManager = TaskSetManager.getInstance(strUserId);

		try {
			// suspend
			if (strOperationType.equalsIgnoreCase("suspend")) {
				objTaskSetManager.suspendTask(strTaskId);
				boolOperationCompleted = true;
			}
			// resume
			if (strOperationType.equalsIgnoreCase("resume")) {
				objTaskSetManager.resumeTask(strTaskId);
				boolOperationCompleted = true;
			}
			// cancel
			if (strOperationType.equalsIgnoreCase("cancel")) {
				objTaskSetManager.cancelTask(strTaskId);
				boolOperationCompleted = true;
			}
			// delete
			if (strOperationType.equalsIgnoreCase("delete")) {
				objTaskSetManager.deleteTask(strTaskId);
				// delete the task's files
				try {
					REUtil.finalizeForTask(strTaskId);
				} catch (Exception excpE) {
					logger.error("SetManagePortlet: doTask_operation: exception caught while doing REUtil.finalizeForTask()");
				}
				boolOperationCompleted = true;
			}
			// delete All
			if (strOperationType.equalsIgnoreCase("deleteAll")) {
				// iterate and delete all
				Task[] arrTasks = objTaskSetManager.getTasksArray();

				for(int i=0; i<arrTasks.length; i++) {
					// get the task id
					strTaskId = Long.toString(arrTasks[i].getIdentity().getValue());

					System.out.println("deleteAll: deleting task " + strTaskId); // debug

					// delete the task
					objTaskSetManager.deleteTask(strTaskId);

					// delete the task's files
					try {
						REUtil.finalizeForTask(strTaskId);
					} catch (Exception excpE) {
						logger.error("SetManagePortlet: doTask_operation: exception caught while doing REUtil.finalizeForTask()");
					}
				}
				boolOperationCompleted = true;
			}

			if (!boolOperationCompleted) {
				strBufContent.append("Warn : Could not perform operation " + strOperationType + ",<br>");
				strBufContent.append("     : on task " + strTaskName + ",<br>");
				strBufContent.append("     : Task does not exist.<br>");
			}

			logger.debug("SetManagePortlet: doTask_operation: Operation completed");

		} catch (Exception excpE) {
			strBufContent.append("Error: Could not perform operation " + strOperationType + ",<br>");
			strBufContent.append("     : on task " + strTaskName + ".<br>");

			logger.error("SetManagePortlet: doTask_operation: Exceptiong caught while trying to");
			logger.error("                                  : perform operation: " + strOperationType);
			logger.error("                                  : on task: " + strTaskName);
			logger.error("                                  : Exception toString: " + excpE.toString());
			logger.error("                                  : Exception message: " + excpE.getMessage());
		}

		if (!boolOperationCompleted) {
			objContext.put("SetManagePortlet_Error", "true");
		}

		objContext.put("SetManagePortlet_Status", strBufContent.toString());
		try { 
			buildNormalContext(null, objContext, objRunData);
		} catch(Exception e) {
		}
//		setTemplate(objRunData, "setManage-view.vm");
		return;
	}

	protected void translateOutput(Task[] arrayTasks) {

		long lTimeDiff = 0;
		String strJobName = null;
		String strStdOutput = null;
		String strStdError = null;
		String strStdOutputPath = null;
		String strStdErrorPath = null;
		String strStdOutputFileName = null;
		String strStdErrorFileName = null;
		FileWriter flFileWriter = null;
		Task objTask = null;

		for (int i = 0; i < arrayTasks.length; i++) {
			objTask = arrayTasks[i];
			/* save time */
			if (objTask.getStatus().getStatus() == Status.COMPLETED) {
				if ((objTask.getCompletedTime() != null) && (objTask.getSubmittedTime() != null)) {
					lTimeDiff = objTask.getCompletedTime().getTimeInMillis() - objTask.getSubmittedTime().getTimeInMillis();
					objTask.setAttribute(TIME_DIFF, Long.toString(lTimeDiff));
				}
			}
			/* save stdout and stderr */
			if ((objTask.getStatus().getStatus() == Status.COMPLETED) && (objTask.getType() == Task.JOB_SUBMISSION) && (((JobSpecification)objTask.getSpecification()).isRedirected())) {

				// check if this has already been done
				strStdOutputPath = (String) objTask.getAttribute(REUtil.RE_STD_OUT_FILE_NAME);
				strStdErrorPath = (String) objTask.getAttribute(REUtil.RE_STD_ERR_FILE_NAME);
				if ((strStdOutputPath != null) && (!strStdOutputPath.equals("")) &&
					(strStdErrorPath != null) && (!strStdErrorPath.equals(""))) {

					// if so continue with the next task
					continue;
				}

				strJobName = objTask.getName();
				strStdOutput = objTask.getStdOutput();
				strStdError = objTask.getStdError();
				if (!((JobSpecification)objTask.getSpecification()).isLocalExecutable()) {
					try {
						REUtil.initForTask(Long.toString(objTask.getIdentity().getValue()));
					} catch (Exception excpE) {
						logger.error("SetManagePortlet: translateOutput: exception caught while doing REUtil.initForTask()");
					}
				}
				try {
					strStdOutputFileName = REUtil.touchFile(Long.toString(objTask.getIdentity().getValue()), REUtil.RE_STD_OUT_FILE_NAME);
					flFileWriter = new FileWriter(strStdOutputFileName);
					flFileWriter.write(REUtil.RE_FILES_HTML_HEADER_1 + REUtil.RE_STD_OUT_HTML_TITLE + REUtil.RE_FILES_HTML_HEADER_2);
					if ((strStdOutput == null) || (strStdOutput.trim().equals(""))) {
						strStdOutput = "No Standard Output";
					}
					flFileWriter.write(strStdOutput);
					flFileWriter.write(REUtil.RE_FILES_HTML_FOTTER);
					flFileWriter.flush();
					flFileWriter.close();

					strStdErrorFileName = REUtil.touchFile(Long.toString(objTask.getIdentity().getValue()), REUtil.RE_STD_ERR_FILE_NAME);
					flFileWriter = new FileWriter(strStdErrorFileName);
					flFileWriter.write(REUtil.RE_FILES_HTML_HEADER_1 + REUtil.RE_STD_ERR_HTML_TITLE + REUtil.RE_FILES_HTML_HEADER_2);
					if ((strStdError == null) || (strStdError.trim().equals(""))) {
						strStdError = "No Standard Error";
					}
					flFileWriter.write(strStdError);
					flFileWriter.write(REUtil.RE_FILES_HTML_FOTTER);
					flFileWriter.flush();
					flFileWriter.close();
				} catch (Exception excpE) {
					logger.error("SetManagePortlet: translateOutput: exception caught while doing REUtil.touchStdOutput/Error()");
				}
				strStdOutputPath = REUtil.REMOTE_EXECUTION_DIR + REUtil.getPathSep() + REUtil.getTaskDir(objTask.getIdentity().getValue())+ REUtil.getPathSep() + REUtil.RE_STD_OUT_FILE_NAME;
				strStdErrorPath = REUtil.REMOTE_EXECUTION_DIR + REUtil.getPathSep() + REUtil.getTaskDir(objTask.getIdentity().getValue())+ REUtil.getPathSep() + REUtil.RE_STD_ERR_FILE_NAME;
				objTask.setAttribute(REUtil.RE_STD_OUT_FILE_NAME, strStdOutputPath);
				objTask.setAttribute(REUtil.RE_STD_ERR_FILE_NAME, strStdErrorPath);
			}
		}
	}

} /* end clsSetManagePortlet */

