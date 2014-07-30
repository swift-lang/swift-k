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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

public class MultipleHandlerTaskModel extends AbstractTaskModel implements TableModel {
	private static final long serialVersionUID = 8282336871287504313L;

	private static final Logger logger = Logger.getLogger(MultipleHandlerTaskModel.class);

	private List handlers;

	private ArrayList tasks;

	public MultipleHandlerTaskModel(List handlers) {
		this.handlers = handlers;
		if (handlers != null) {
			tasks = new ArrayList();
			Iterator i = handlers.iterator();
			while (i.hasNext()) {
				tasks.addAll(((TaskHandler) i.next()).getAllTasks());
			}
		}
	}

	public int getRowCount() {
		if (tasks == null) {
			return 0;
		}
		return tasks.size();
	}

	public int getColumnCount() {
		return 5;
	}

	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
			case 0:
				return "ID";
			case 1:
				return "Type";
			case 2:
				return "Details";
			case 3:
				return "Host";
			case 4:
				return "Status";
			default:
				return "Unknown";
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
		if (rowIndex >= tasks.size()) {
			return "";
		}
		Task task = (Task) tasks.get(rowIndex);
		switch (columnIndex) {
			case 0:
				return "" + task.getIdentity().getValue();
			case 1:
				if (task.getType() == Task.JOB_SUBMISSION) {
					return "Job";
				}
				if (task.getType() == Task.FILE_TRANSFER) {
					return "Transfer";
				}
				return "Info";
			case 2:
				if (task.getSpecification() instanceof JobSpecification) {
					JobSpecification js = (JobSpecification) task.getSpecification();
					return js.getExecutable() + " " + js.getArguments();
				}
				if (task.getSpecification() instanceof FileTransferSpecification) {
					FileTransferSpecification fs = (FileTransferSpecification) task.getSpecification();
					return fs.getSourceDirectory() + "/" + fs.getSourceFile() + " -> "
							+ fs.getDestinationDirectory() + "/" + fs.getDestinationFile();
				}
			case 3:
				return task.getService(Service.DEFAULT_SERVICE).getServiceContact().getHost();
			case 4:
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
			default:
				return null;
		}
	}

	public void update() {
		if (handlers == null) {
			logger.warn("Handler is null");
			return;
		}
		tasks = new ArrayList();
		Iterator i = handlers.iterator();
		while (i.hasNext()) {
			tasks.addAll(((TaskHandler) i.next()).getAllTasks());
		}
		if (tasks != null) {
			fireTableRowsUpdated(0, tasks.size() - 1);
		}
		else {
			logger.warn("Tasks is null");
		}
	}

	public void setTaskHandlers(List th) {
		handlers = th;
		update();
		fireTableStructureChanged();
	}

	public Task getTaskAtRow(int row) {
		if ((tasks != null) && (row < tasks.size())) {
			return (Task) tasks.get(row);
		}
		return null;
	}
}
