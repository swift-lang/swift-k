
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant.taskdefs;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Task;

/**
 * Implements a for loop. Arguments are "from" and "to".
 * A special case is when the from and to strings have the same length
 * In this case, if required, the values for the iterator will be zero
 * padded. Eample
 * <for name="var" from="01" to "12">
 * ...
 * </for>
 * This will generate the values 01, 02, 03, ... 12
 */
public class For extends Task implements ETaskContainer{
	private List tasks;
	private String name;
	private String from;
	private String to;
	
	public For(){
		tasks = new LinkedList();
		name = null;
		from = null;
		to = null;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setFrom(String from) {
		this.from = from;
	}
	
	public String getFrom() {
		return from;
	}
	
	public void setTo(String to) {
		this.to = to;
	}
	
	public String getTo() {
		return to;
	}
		
	public void addTask(Task task){
		tasks.add(task);
	}
	
	public List getTasks(){
		return tasks;
	}
	
	public void execute() throws BuildException{
		if ((name == null) || (from == null) || (to == null)){
			throw new BuildException("Invalid parameters for <for>...</for>");
		}
		boolean keeplen = from.length() == to.length();
		int f = Integer.parseInt(from);
		int t = Integer.parseInt(to);
		for (int i = f; i <= t; i++){
			String s = String.valueOf(i);
			if (keeplen){
				while(s.length() < from.length()){
					s = "0"+s;
				}
			}
			getProject().setProperty(name, s);
			Iterator j = tasks.iterator();
			while (j.hasNext()){
				Task task = (Task) j.next();
				//the following line took me 3 hours
				ProjectHelper.configure(task, task.getRuntimeConfigurableWrapper().getAttributes(), task.getProject());
				task.perform();
			}
		}
	}
}

