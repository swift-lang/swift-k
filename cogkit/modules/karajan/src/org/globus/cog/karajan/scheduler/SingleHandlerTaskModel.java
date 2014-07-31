/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Aug 7, 2003
 */
package org.globus.cog.karajan.scheduler;

import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

public class SingleHandlerTaskModel extends AbstractTaskModel implements TableModel {
	private static final long serialVersionUID = -1342313893277868828L;

	private static final Logger logger = Logger.getLogger(SingleHandlerTaskModel.class);

	private TaskHandler handler;

	private Task[] tasks;

	public SingleHandlerTaskModel(TaskHandler handler) {
		this.handler = handler;
		if (handler != null) {
			tasks = (Task[]) handler.getAllTasks().toArray(new Task[0]);
		}
	}

	public int getRowCount() {
		if (tasks == null) {
			return 0;
		}
		return tasks.length;
	}

	public int getColumnCount() {
		return 5;
	}

	public static final String[] COLUMN_NAMES = new String[] { "ID", "Type", "Details", "Host",
			"Status" };

	public String getColumnName(int columnIndex) {
		try {
			return COLUMN_NAMES[columnIndex];
		}
		catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Column index > "+COLUMN_NAMES.length);
		}
	}

	public Class getColumnClass(int columnIndex) {
		return String.class;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (tasks == null) {
			return null;
		}
		if (rowIndex >= tasks.length) {
			return "";
		}
		Task task = tasks[rowIndex];
		switch (columnIndex) {
			case 0:
				return getTaskID(task);
			case 1:
				return getTaskType(task);
			case 2:
				return getTaskDetails(task);
			case 3:
				return getTaskHost(task);
			case 4:
				return getTaskStatus(task);
			default:
				return "Unknown";
		}
	}

	private String getTaskID(Task task) {
		return String.valueOf(task.getIdentity().getValue());
	}

	private String getTaskType(Task task) {
		if (task.getType() == Task.JOB_SUBMISSION) {
			return "Job";
		}
		if (task.getType() == Task.FILE_TRANSFER) {
			return "Transfer";
		}
		return "Info";
	}

	private String getTaskDetails(Task task) {
		if (task.getSpecification() instanceof JobSpecification) {
			JobSpecification js = (JobSpecification) task.getSpecification();
			return js.getExecutable() + " " + js.getArguments();
		}
		if (task.getSpecification() instanceof FileTransferSpecification) {
			FileTransferSpecification fs = (FileTransferSpecification) task.getSpecification();
			return fs.getSourceDirectory() + "/" + fs.getSourceFile() + " -> "
					+ fs.getDestinationDirectory() + "/" + fs.getDestinationFile();
		}
		return "";
	}

	private String getTaskHost(Task task) {
		return task.getService(Service.DEFAULT_SERVICE).getServiceContact().getHost();
	}

	private String getTaskStatus(Task task) {
		switch (task.getStatus().getStatusCode()) {
			case Status.ACTIVE:
				return "Active";
			case Status.CANCELED:
				return "Canceled";
			case Status.COMPLETED:
				return "Completed";
			case Status.FAILED:
				return "Failed";
			case Status.RESUMED:
				return "Resumed";
			case Status.SUBMITTED:
				return "Submitted";
			case Status.SUSPENDED:
				return "Suspended";
			case Status.UNSUBMITTED:
				return "Unsubmitted";
			default:
				return "Unknown";
		}
	}

	public void update() {
		if (handler == null) {
			logger.warn("Handler is null");
			return;
		}
		tasks = (Task[]) handler.getAllTasks().toArray(new Task[0]);
		if (tasks != null) {
			fireTableRowsUpdated(0, tasks.length - 1);
		}
		else {
			logger.warn("Tasks is null");
		}
	}

	public void setTaskHandler(TaskHandler th) {
		handler = th;
		update();
		fireTableStructureChanged();
	}

	public Task getTaskAtRow(int row) {
		if ((tasks != null) && (row < tasks.length)) {
			return tasks[row];
		}
		return null;
	}
}
