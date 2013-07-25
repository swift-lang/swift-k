/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.swing;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.table.AbstractTableModel;

import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;
import org.griphyn.vdl.karajan.monitor.StatefulItemClassSet;
import org.griphyn.vdl.karajan.monitor.common.StatefulItemModel;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.TaskItem;

public class FilteringTaskTable extends JScrollPane {
	private JTable table;
	private String name;
	private StatefulItemClassSet<TaskItem> items;
	private int taskType;

	public FilteringTaskTable(String name, StatefulItemClassSet<TaskItem> itemClassSet, int taskType) {
		super(new JTable());
		this.table = (JTable) super.getViewport().getView();
		this.name = name;
		this.items = itemClassSet;
		this.taskType = taskType;
		this.setBorder(BorderFactory.createTitledBorder(
		    BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), name));
		this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		table.setModel(newModel(taskType));
		table.setDefaultRenderer(TransferProgress.class, new TransferProgressRenderer());
	}

	protected Model newModel(int taskType) {
		switch (taskType) {
			case Task.JOB_SUBMISSION:
				return new JobModel(items);
			case Task.FILE_TRANSFER:
				return new TransferModel(items);
			case Task.FILE_OPERATION:
				return new FileopModel(items);
			default:
				return new Model(items, -1);
		}
	}

	public Model getTableModel() {
		return (Model) table.getModel();
	}

	public void dataUpdated() {
		getTableModel().invalidate();
		table.setSize(table.getPreferredSize());
	}

	public static class Model extends AbstractTableModel implements StatefulItemModel {
		private boolean dirty;
		private List<Task> rows;
		private List<TaskItem> itl;
		private StatefulItemClassSet<TaskItem> items;
		private int taskType;

		public Model(StatefulItemClassSet<TaskItem> items, int taskType) {
			dirty = true;
			this.items = items;
			this.taskType = taskType;
		}

		public void invalidate() {
			dirty = true;
		}

		public int getColumnCount() {
			return 1;
		}

		private synchronized void update() {
			rows = new ArrayList<Task>();
			itl = items.getAll();
			for (TaskItem t : itl) {
				Task task = t.getTask();
				if (taskType == -1 || task.getType() == taskType) {
					rows.add(task);
				}
			}
			dirty = false;
		}

		public int getRowCount() {
			update();
			return rows.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			update();
			return rows.get(rowIndex);
		}
		
		public StatefulItem getItem(int rowIndex) {
			return itl.get(rowIndex);
		}

		public void fireTableDataChanged() {
			dirty = true;
			super.fireTableDataChanged();
		}
		
		public void shutdown() {
			
		}
	}

	public static class JobModel extends Model {
		
		public JobModel(StatefulItemClassSet<TaskItem> items) {
			super(items, Task.JOB_SUBMISSION);
		}
		
		public int getColumnCount() {
			return 4;
		}

		public String getColumnName(int column) {
			switch (column) {
				case 0:
					return "Executable";
				case 1:
					return "Arguments";
				case 2:
					return "Host";
				case 3:
					return "Status";
				default:
					return "?";
			}
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Task task = (Task) super.getValueAt(rowIndex, columnIndex);
			JobSpecification spec = (JobSpecification) task.getSpecification();
			switch (columnIndex) {
				case 0:
					return spec.getExecutable();
				case 1:
					return spec.getArguments();
				case 2:
					return task.getService(0);
				case 3:
					return task.getStatus().getStatusString();
				default:
					return "What do you want from me?";
			}
		}
	}

	private static Timer transferUpdateTimer = new Timer();

	public static class TransferModel extends Model {

		public TransferModel(StatefulItemClassSet<TaskItem> items) {
			super(items, Task.FILE_TRANSFER);
			transferUpdateTimer.schedule(new TimerTask() {
				public void run() {
					TransferModel.this.fireTableDataChanged();
				}
			}, 1000, 1000);
		}

		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 3) {
				return TransferProgress.class;
			}
			else {
				return super.getColumnClass(columnIndex);
			}
		}
		
		public int getColumnCount() {
			return 3;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Task task = (Task) super.getValueAt(rowIndex, columnIndex);
			FileTransferSpecification spec = (FileTransferSpecification) task.getSpecification();
			switch (columnIndex) {
				case 0:
					return task.getService(0) + "/" + spec.getSourceDirectory() + "/" + spec.getSourceFile();
				case 1:
					return task.getService(1) + "/" + spec.getDestinationDirectory() + "/" + spec.getDestinationFile();
				case 2:
					return new TransferProgress(task.getStdOutput());
				default:
					return "?";
			}
		}

		public String getColumnName(int column) {
			switch (column) {
				case 0:
					return "Source";
				case 1:
					return "Destination";
				case 2:
					return "Progress";
				default:
					return "?";
			}
		}
	}

	public class FileopModel extends Model {
		
		public FileopModel(StatefulItemClassSet<TaskItem> items) {
			super(items, Task.FILE_OPERATION);
		}

		public int getColumnCount() {
			return 3;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Task task = (Task) super.getValueAt(rowIndex, columnIndex);
			FileOperationSpecification spec = (FileOperationSpecification) task.getSpecification();
			switch (columnIndex) {
				case 0:
					return spec.getOperation();
				case 1:
					return spec.getArguments();
				case 2:
					return task.getService(0);
				default:
					return "?";
			}
		}

		public String getColumnName(int column) {
			switch (column) {
				case 0:
					return "Type";
				case 1:
					return "Arguments";
				case 2:
					return "Host";
				default:
					return "?";
			}
		}

	}
}
