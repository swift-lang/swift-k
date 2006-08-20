//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 2, 2004
 */
package org.globus.cog.karajan.util;

import org.globus.cog.karajan.scheduler.TaskConstraints;

public abstract class Contact {
	private static int idcounter = 0;
	private final int id;
	private TaskConstraints constraints;
	
	public Contact() {
		id = idcounter++;
	}
	
	public boolean equals(Object obj){
		if (obj instanceof Contact){
			return id == ((Contact) obj).id;
		}
		return false;
	}
	
	public int hashCode(){
		return id;
	}

	public int getId() {
		return id;
	}
	
	public abstract boolean isVirtual();

	public TaskConstraints getConstraints() {
		return constraints;
	}

	public void setConstraints(TaskConstraints constraints) {
		this.constraints = constraints;
	}
}
