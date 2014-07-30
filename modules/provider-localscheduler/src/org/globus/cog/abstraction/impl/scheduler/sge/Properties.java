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
package org.globus.cog.abstraction.impl.scheduler.sge;

import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;

public class Properties extends AbstractProperties {

	private static final long serialVersionUID = 1L;
	public static final String PROPERTIES = "provider-sge.properties";
	public static final String POLL_INTERVAL = "poll.interval";
	public static final String QSUB = "qsub";
	public static final String QSTAT = "qstat";
	public static final String QDEL = "qdel";
	public static final String QCONF = "qconf";
	public static final String DEFAULT_PE = "parallel.environment"; 
	public static final String SUBMISSION_DELAY = "submission.delay";
	private static Properties properties;

	/**
	 * getProperties - return properties
	 * @return Properties object representing SGE properties
	 */
	public static synchronized Properties getProperties() {
		if (properties == null) {
			properties = new Properties();
			properties.load(PROPERTIES);
		}
		return properties;
	}

	/**
	 * Get the config command
	 * @return String with config command
	 */
	public String getConfigCommand() {
		return QCONF;
	}
	/**
	 * getDefaultPE - Get the default parallel environment
	 * @return String containing pe
	 */
	public String getDefaultPE() {
	    return getProperty(DEFAULT_PE);
	}

	/**
	 * getPollCommandName - Get poll command name
	 * @return String containing poll command
	 */
	public String getPollCommandName() {
		return QSTAT;
	}

	/**
	 * getRemoveCommandName - Get remove command
	 * @return String of command on how to remove a job
	 */
	public String getRemoveCommandName() {
		return QDEL;
	}

	/**
	 * getSubmissionDelay - Get length to sleep before submitting a job
	 * Value as a string representing milliseconds
	 * @return Submission delay as String in milliseconds
	 */
	public String getSubmissionDelay() {
		return getProperty(SUBMISSION_DELAY);
	}
	
	/**
	 * getSubmitCommandName - Get submit command
	 * @return String of submit command
	 */
	public String getSubmitCommandName() {
		return QSUB;
	}

	/**
	 * Set the default config command
	 * Used for gathering information about the queues
	 * @param config String with command name
	 */
	public void setConfigCommand(String config) {
		setProperty(QCONF, config);
	}
	/**
	 * setDefaultPE - set the default parallel environment
	 * @param pe String representing pe
	 */
	public void setDefaultPE(String pe) {
	    setProperty(DEFAULT_PE, pe);
	}

	/**
	 * setDefault - Reset all SGE options to default
	 */
	protected void setDefaults() {
		setPollInterval(10);
		setSubmitCommand("qsub");
		setPollCommand("qstat -xml");
		setRemoveCommand("qdel");
		setDefaultPE("threaded");
		setConfigCommand("qconf");
		setSubmissionDelay("0");
	}

	/**
	 * setSubmissionDelay - set the submission delay
	 * @param delay String representing milliseconds to sleep 
	 */
	private void setSubmissionDelay(String delay) {
		setProperty(SUBMISSION_DELAY, delay);
	}
}
