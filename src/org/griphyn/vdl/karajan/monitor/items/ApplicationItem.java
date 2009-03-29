/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.items;

import java.util.Date;

public class ApplicationItem extends AbstractStatefulItem {
	private String name, arguments, host;
	private Date startTime;

	public ApplicationItem(String id, String name, String arguments, String host, Date startTime) {
		super(id);
		this.name = name;
		this.arguments = arguments;
		this.host = host;
		this.startTime = startTime;
	}
	
	public ApplicationItem(String id) {
		this(id, null, null, null, null);
	}

	public StatefulItemClass getItemClass() {
		return StatefulItemClass.APPLICATION;
	}

	public String getArguments() {
		return arguments;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public String getName() {
		return name;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public String toString() {
		return "APP[" + name + ", " + arguments + ", " + host + "]";
	}
}
