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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 20, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.shell;

// import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;

public class Properties extends AbstractProperties {

    private static final long serialVersionUID = 1L;
	
	public static final String POLL_INTERVAL = "poll.interval";
	public static final String SUBMIT_COMMAND = "submit.command";
	public static final String POLL_COMMAND = "poll.command";
	public static final String CANCEL_COMMAND = "cancel.command";
	public static final String NAME = "name";

	public static synchronized Properties getProperties(String name) {
	    Properties properties = new Properties();
		properties.load("provider-" + name + ".properties");
		properties.put(NAME, name);
		return properties;
	}
	
	protected void setDefaults() {
		setPollInterval(5);
	}


	public String getPollCommandName() {
		return POLL_COMMAND;
	}


	public String getRemoveCommandName() {
		return CANCEL_COMMAND;
	}


	public String getSubmitCommandName() {
		return SUBMIT_COMMAND;
	}

	public String getName() {
	    return getProperty(NAME);
	}
}
