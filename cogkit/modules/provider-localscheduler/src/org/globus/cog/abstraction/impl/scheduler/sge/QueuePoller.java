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
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.sge;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;

/**
 * SGE queue poller class
 */
public class QueuePoller extends AbstractQueuePoller {

	private static String[] CMDARRAY;
	public static final Logger logger = Logger.getLogger(QueuePoller.class);
	private Set<Object> processed;
	private Hashtable<String, QueueInformation> queueInformation;
	DocumentBuilder builder;
	Document doc;
	
	public QueuePoller(AbstractProperties properties) {
		this("SGE provider queue poller", properties);
	}

	public QueuePoller(String name, AbstractProperties properties) {
		super(name, properties);
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		}
		catch(Exception e) {
			if(logger.isInfoEnabled()) {
				logger.info(e.getMessage());
			}
		}
		processed = new HashSet<Object>();
		gatherQueueInformation();
	}
	
	/**
	 * getDataFromElement - Make XML parsing a bit easier 
	 * @param e XML Element
	 * @return XML data as a String
	 */
	public static String getDataFromElement(Element e) {
		try {
			Node child = e.getFirstChild();
			if (child instanceof CharacterData) {
				CharacterData cd = (CharacterData) child;
				return cd.getData();
			}
		}
		
		catch (Exception ex) {
			logger.debug("Error in getDataFromElement");
			logger.debug(ex.getMessage());
			logger.debug(ex.getStackTrace());
		}
		return "";
	}


	/** 
	 * gatherQueueInformation - Collect information about queues and PEs
	 */
	private void gatherQueueInformation() {
		queueInformation = new Hashtable<String, QueueInformation>();
		String command[] = {
				((Properties) this.getProperties()).getConfigCommand(), "-sql" };

		try {
			// Get queue names
			Process p = Runtime.getRuntime().exec(command);
			InputStream is = p.getInputStream();
			try {
				p.waitFor();
			} catch (InterruptedException e1) {
				logger.error("QueuePoller command failed");
				logger.error(e1.getMessage());
			}

			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = "";

			while ((line = br.readLine()) != null) {
				queueInformation.put(line, new QueueInformation());
			}

			// Get info about each queue
			for (String queue : queueInformation.keySet()) {

				command = new String[] {
						((Properties) this.getProperties()).getConfigCommand(),
						"-sq", queue };
				p = Runtime.getRuntime().exec(command);
				try {
					p.waitFor();
				} catch (InterruptedException e) {
					logger.error("QueuePoller command interrupted");
					logger.error(e.getMessage());
				}
				is = p.getInputStream();
				isr = new InputStreamReader(is);
				br = new BufferedReader(isr);

				while ((line = br.readLine()) != null) {
					String results[] = line.split("\\s+", 2);
					queueInformation.get(queue).addData(results);
				}
				
				p.destroy();
			}
		} catch (IOException e) {
			logger.error("QueuePoller command interrupted");
			logger.error(e.getMessage());
			logger.error(e.getStackTrace());
		}
	}

	/**
	 * getAllQueues - Get a list of queues in a list
	 * @return ArrayList<String> of queues
	 */
	public ArrayList<String> getAllQueues() {
		ArrayList<String> result = new ArrayList<String>();
		for (String s : queueInformation.keySet()) {
			result.add(s);
		}
		return result;
	}

	/**
	 * getCMDArray - Return poll command
	 * @return String array contains poll command and flags
	 */
	protected synchronized String[] getCMDArray() {
		if (CMDARRAY == null) {
			CMDARRAY = getProperties().getPollCommand().split(" ");
		}
		return CMDARRAY;
	}

	/**
	 * Return queue information for a requested queue
	 * @param queue
	 *            String of queue name
	 * @return QueueInformation for requested queue
	 */
	public QueueInformation getQueueInformation(String queue) {
		return queueInformation.get(queue);
	}

	/**
	 * isValidQueue - Determine if queue is valid on this system
	 * @param queue Queue name
	 * @return True if queue exists, false otherwise
	 */
	public boolean isValidQueue(String queue) {
		if (queueInformation.keySet().contains(queue))
			return true;
		else
			return false;
	}

	/**
	 * processStderr - defines how to handle errors from the queue poller 
	 * @param InputStream
	 */
	protected void processStderr(InputStream is) throws IOException {
		String error = new Scanner(is).useDelimiter("\\A").next();
		if(logger.isDebugEnabled()) {
			logger.debug("QueuePoller error: " + error);
		}
	}

	/**
	 * processStdout - Process poller output and determine the status of jobs
	 * Uses XML to parse, which requires SGE 6.0 or later
	 * @param InputStream is - stream representing output
	 */
	protected void processStdout(InputStream is) throws IOException {
		try {
			String xml = new Scanner(is).useDelimiter("\\A").next();
			if(logger.isDebugEnabled()) {
				logger.debug("QueuePoller XML: " + xml);
			}	
			InputStream is_copy = new ByteArrayInputStream(xml.getBytes());
			doc = builder.parse(is_copy);
			processed.clear();
			NodeList nodes = doc.getElementsByTagName("job_list");
			Job tmpJob;

			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);
				NodeList nodeList = element.getElementsByTagName("JB_job_number");
				Element line = (Element) nodeList.item(0);
				String jobid = getDataFromElement(line);
				tmpJob = getJob(jobid);

				if (tmpJob == null) {
					continue;
				}

				processed.add(jobid);
				nodeList = element.getElementsByTagName("state");
				line = (Element) nodeList.item(0);
				String state = getDataFromElement(line);

				if (state.contains("q") || state.contains("w")) {
					if (logger.isDebugEnabled()) {
						logger.debug(jobid + " is queued");
					}
					tmpJob.setState(Job.STATE_QUEUED);
				} else if (state.contains("r")) {
					if (logger.isDebugEnabled()) {
						logger.debug(jobid + " is running");
					}
					tmpJob.setState(Job.STATE_RUNNING);
				} else if (state.contains("E")) {
					tmpJob.fail("Job is in an error state. Try running qstat -j "
						+ jobid + " to see why.");
				}
			}

			Iterator i = getJobs().entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry e = (Map.Entry) i.next();
				String id = (String) e.getKey();
				Job job = (Job) e.getValue();
				if (!processed.contains(id)) {
					if (logger.isDebugEnabled()) {
						logger.debug(id + " is done");
					}
					job.setState(Job.STATE_DONE);
					if (job.getState() == Job.STATE_DONE) {
						addDoneJob(id);
					} else {
						// at least on Ranger the job is done long
						// before qstat reports it as done, so check
						// if the exit code file is there
						File f = new File(job.getExitcodeFileName());
						if (f.exists()) {
							job.setState(Job.STATE_DONE);
							if (job.getState() == Job.STATE_DONE) {
								addDoneJob(id);
							}
						}
                   }
				}
			}
		}
		catch (Exception e) {
			if(logger.isDebugEnabled()) {
				logger.debug("Exception in processStdout");
				logger.debug(e.getStackTrace());
			}
		}
	}
}
