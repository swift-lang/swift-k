/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.items;

import org.globus.cog.abstraction.interfaces.Task;

public class TaskItem extends AbstractStatefulItem {
	private Task task;
	private int status;
	
	public TaskItem(String id, Task task) {
		super(id);
		this.task = task;
	}
	
	public TaskItem(String id) {
		this(id, null);
	}
	
	public StatefulItemClass getItemClass() {
		return StatefulItemClass.TASK;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public String toString() {
		return task.toString();
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
}
