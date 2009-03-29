/*
 * Created on Jan 30, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.swing;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

import org.globus.cog.abstraction.interfaces.Task;
import org.griphyn.vdl.karajan.monitor.StatefulItemClassSet;

public class TasksRenderer extends JSplitPane implements ClassRenderer {
	private String name;
	private StatefulItemClassSet set;
	private FilteringTaskTable jobs, transfers, fileops;

	public TasksRenderer(String name, StatefulItemClassSet set) {
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
