// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------



package org.globus.cog.portlet;

import org.globus.cog.core.impl.common.IdentityImpl;
import org.globus.cog.core.impl.common.TaskSetImpl;
//import org.globus.cog.core.impl.common.TaskSetHandlerImpl;
import org.globus.cog.core.impl.common.ThreadedTaskSetHandlerImpl;
import org.globus.cog.core.interfaces.Identity;
import org.globus.cog.core.interfaces.SetStatus;
import org.globus.cog.core.interfaces.Status;
import org.globus.cog.core.interfaces.Task;
import org.globus.cog.core.interfaces.TaskSet;
import org.globus.cog.core.interfaces.TaskSetHandler;

import org.apache.log4j.Logger;

import java.util.Enumeration;
import java.util.Hashtable;


public class TaskSetManager {

	// setup logging
	static Logger logger = Logger.getLogger(TaskSetManager.class.getName());

	private static Hashtable _hashTable;

	private String _strUserId;
	private Identity _objTaskId;
	private TaskSet _objTaskSet;
	private TaskSetHandler _objTaskSetHandler;

	private boolean _boolSubmitted;

	protected TaskSetManager(String strUserId) {
		// creator
		logger.debug("TaskSetManager: Creator...");

		// initialize
		this._boolSubmitted = false;
		this._strUserId = strUserId;
		this._objTaskId = new IdentityImpl();
		this._objTaskSet = new TaskSetImpl();
//		this._objTaskSetHandler = new TaskSetHandlerImpl();
		this._objTaskSetHandler = new ThreadedTaskSetHandlerImpl();
	}

/*
	protected void setRunning() {
		this._boolRunning = true;
	}

	protected boolean getRunning() {
		return this._boolRunning;
	}
*/

	public static TaskSetManager getInstance(String strUserId) {
		TaskSetManager objTaskSetManager = null;

		logger.debug("TaskSetManager: getInstance: strUserId: " + strUserId);

		if (_hashTable == null) {
			_hashTable = new Hashtable();
		}
		try {
			if (!_hashTable.containsKey(strUserId)) {

				logger.debug("TaskSetManager: getInstance: creating a new instance for user");

				objTaskSetManager = new TaskSetManager(strUserId);

				logger.debug("TaskSetManager: getInstance: created.");

				_hashTable.put(strUserId, objTaskSetManager);
			} else {
				logger.debug("TaskSetManager: getInstance: returning old instance");

				objTaskSetManager = (TaskSetManager)_hashTable.get(strUserId);
			}
		} catch (NullPointerException excpNPE) {
			logger.error("TaskSetManager: getInstance: Null Pointer Exception");
		}

		return objTaskSetManager;
	}

/*
	public void run() {
		try {
			this._objTaskSetHandler.submit(this._objTaskSet);
		} catch (Exception excpE) {
			excpE.printStackTrace();
		}
	}
*/

	public void addTask(Task objTask) 
		throws Exception {

		logger.debug("TaskSetManager: addTask: adding a NEW task for user " + this._strUserId);

		if (objTask == null) {
			throw new Exception("TaskSetManager: addTask: Task cannot be null.");
		} else {
			this._objTaskSet.add(objTask);
			if (!this._boolSubmitted) {

				logger.debug("TaskSetManager: addTask: submitting TaskSet");

				try {
					this._objTaskSetHandler.submit(this._objTaskSet);
				} catch (Exception excpE) {
					excpE.printStackTrace();
				}
				this._boolSubmitted = true;
			}
		}
	}

	public TaskSetHandler getTaskSetHandler() {
		return this._objTaskSetHandler;
	}

	public Task[] getTasksArray() {
		return this._objTaskSet.toArray();
	}

	public TaskSet getTaskSet() {
		return this._objTaskSet;
	}

	public void suspendTask(String strTaskId)
		throws Exception {

		Task objTask = null;
		Identity objId = this._objTaskId;

		objId.setValue(Long.parseLong(strTaskId));
		if (this._objTaskSet.contains(objId)) {
			objTask = this._objTaskSet.get(objId);
		}
		
		this._objTaskSetHandler.suspend(objTask);
	}

	public void resumeTask(String strTaskId)
		throws Exception {

		Task objTask = null;
		Identity objId = this._objTaskId;

		objId.setValue(Long.parseLong(strTaskId));
		if (this._objTaskSet.contains(objId)) {
			objTask = this._objTaskSet.get(objId);
		}
		
		this._objTaskSetHandler.resume(objTask);
	}

	public void cancelTask(String strTaskId)
		throws Exception {

		Task objTask = null;
		Identity objId = this._objTaskId;

		objId.setValue(Long.parseLong(strTaskId));
		if (this._objTaskSet.contains(objId)) {
			objTask = this._objTaskSet.get(objId);
		}
		
		this._objTaskSetHandler.cancel(objTask);
	}

	public void deleteTask(String strTaskId)
		throws Exception {

		logger.debug("TaskSetManager: deleteTask: strTaskId: " + strTaskId);

		int iStatus = -1;
		Task objTask = null;
		Identity objId = this._objTaskId;

		objId.setValue(Long.parseLong(strTaskId));

		logger.debug("TaskSetManager: deleteTask: strTaskId: " + objId.getValue());
		logger.debug("TaskSetManager: deleteTask: size of TaskSet: " + this._objTaskSet.getSize());

//		this._objTaskSet.remove(objId);

		if (this._objTaskSet.contains(objId)) {

			// if the status is not FAILED or CANCELLED or COMPLETED
			// then first cancel the task and then delete
			iStatus = this._objTaskSet.get(objId).getStatus().getStatus();

			switch (iStatus) {
				case Status.UNSUBMITTED:
				case Status.SUBMITTED:
				case Status.ACTIVE:
				case Status.SUSPENDED:
				case Status.RESUMED:
					objTask = this._objTaskSet.get(objId);
					this._objTaskSetHandler.cancel(objTask);
					break;
				default:
					break;
			}

			this._objTaskSet.remove(objId);

			logger.debug("TaskSetManager: deleteTask: removed " + strTaskId);
		}

		logger.debug("TaskSetManager: deleteTask: size of TaskSet: " + this._objTaskSet.getSize());
	}
}
