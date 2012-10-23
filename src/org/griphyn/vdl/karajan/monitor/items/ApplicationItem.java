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
