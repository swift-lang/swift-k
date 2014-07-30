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
package org.globus.cog.abstraction.impl.scheduler.cobalt;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;

public class Properties extends AbstractProperties {
	private static Logger logger = Logger.getLogger(Properties.class);

	public static final String PROPERTIES = "provider-cobalt.properties";
	
	
	public static final String CQSUB = "cqsub";
	public static final String CQSTAT = "cqstat";
	public static final String CQDEL = "cqdel";
	public static final String EXITCODE_REGEXP = "exitcode.regexp";

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
		setSubmitCommand("cqsub");
		setPollCommand("cqstat");
		setRemoveCommand("cqdel");
		setExitcodeRegexp("(?:.*BG/. job exit status =\\s*([0-9]+))|(?:.*exit status = \\(([0-9]+)\\))");
	}

	
	public String getExitcodeRegexp() {
		return getProperty(EXITCODE_REGEXP);
	}
	
	public void setExitcodeRegexp(String r) {
		setProperty(EXITCODE_REGEXP, r);
	}

	public String getPollCommandName() {
		return CQSTAT;
	}

	public String getRemoveCommandName() {
		return CQDEL;
	}

	public String getSubmitCommandName() {
		return CQSUB;
	}
	
	
}
