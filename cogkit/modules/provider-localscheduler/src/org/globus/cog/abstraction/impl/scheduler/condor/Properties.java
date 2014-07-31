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
package org.globus.cog.abstraction.impl.scheduler.condor;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;

public class Properties extends AbstractProperties {
	private static final long serialVersionUID = 1L;

	public static final String PROPERTIES = "provider-condor.properties";
	
	public static final String POLL_INTERVAL = "poll.interval";
	public static final String CONDOR_SUBMIT = "condor_submit";
	public static final String CONDOR_Q = "condor_q";
	public static final String CONDOR_RM = "condor_rm";
	public static final String CONDOR_QEDIT = "condor_qedit";

	private static Properties properties;

	public static synchronized Properties getProperties() {
		if (properties == null) {
			properties = new Properties();
			properties.load(PROPERTIES);
		}
		return properties;
	}

	protected void setDefaults() {
		setPollInterval(5);
		setSubmitCommand("condor_submit");
		setPollCommand("condor_q");
		setRemoveCommand("condor_rm");
		setProperty(CONDOR_QEDIT, "condor_qedit");
	}

	public String getPollCommandName() {
		return CONDOR_Q;
	}

	public String getRemoveCommandName() {
		return CONDOR_RM;
	}

	public String getSubmitCommandName() {
		return CONDOR_SUBMIT;
	}
	
	
}
