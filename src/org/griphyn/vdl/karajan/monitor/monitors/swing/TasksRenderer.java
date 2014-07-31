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
 * Created on Jan 30, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.swing;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

import org.globus.cog.abstraction.interfaces.Task;
import org.griphyn.vdl.karajan.monitor.StatefulItemClassSet;
import org.griphyn.vdl.karajan.monitor.items.TaskItem;

public class TasksRenderer extends JSplitPane implements ClassRenderer {
	private String name;
	private StatefulItemClassSet<TaskItem> set;
	private FilteringTaskTable jobs, transfers, fileops;

	public TasksRenderer(String name, StatefulItemClassSet<TaskItem> set) {
		super(VERTICAL_SPLIT);
		this.name = name;
		this.set = set;
		this.setTopComponent(createJobTable());
		this.setBottomComponent(createFileStuff());
		this.setResizeWeight(0.3333333333333333333333333333333333333333333333333333333);// yiiihaaaaa!
	}

	protected JComponent createJobTable() {
		return jobs = new FilteringTaskTable("Jobs", set, Task.JOB_SUBMISSION);
	}

	protected JComponent createFileStuff() {
		JSplitPane t = new JSplitPane(VERTICAL_SPLIT);
		t.setResizeWeight(0.5);
		t.setTopComponent(createTransferTable());
		t.setBottomComponent(createFileopTable());
		return t;
	}

	protected JComponent createTransferTable() {
		return transfers = new FilteringTaskTable("Transfers", set, Task.FILE_TRANSFER);
	}

	protected JComponent createFileopTable() {
		return fileops = new FilteringTaskTable("File Operations", set, Task.FILE_OPERATION);
	}

	public void dataChanged() {
		jobs.dataUpdated();
		transfers.dataUpdated();
		fileops.dataUpdated();
		this.repaint();
	}
}
