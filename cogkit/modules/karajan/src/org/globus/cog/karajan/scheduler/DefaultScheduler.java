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

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.scheduler;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Contact;
import org.globus.cog.karajan.util.ContactSet;
import org.globus.cog.karajan.util.TypeUtil;

public class DefaultScheduler extends LateBindingScheduler implements Scheduler, Runnable {
	private static final Service[] SERVICEARRAY = new Service[0];

	private static final Contact[] CONTACTARRAY = new Contact[0];

	private static final Logger logger = Logger.getLogger(DefaultScheduler.class);

	private static String[] propertyNames = new String[] { "jobsPerCpu", "maxSimultaneousJobs",
			"showTaskList" };

	private int contactCursor;

	private boolean showTaskList;

	private TaskList tl;

	public DefaultScheduler() {
		contactCursor = 0;
		showTaskList = false;
		setName("Default Scheduler");
	}

	public void setTaskList(boolean value) {
		if (!showTaskList && value) {
			activateTaskList();
		}
		if (showTaskList && !value) {
			deactivateTaskList();
		}
		showTaskList = value;
	}

	public void activateTaskList() {
		tl = new TaskList(getTaskHandlers());
		tl.setVisible(true);
		tl.update(getJobQueue().size(), getRunning());
	}

	public void deactivateTaskList() {
		tl.setVisible(false);
		tl.dispose();
		tl = null;
	}

	protected synchronized BoundContact getNextContact(TaskConstraints t)
			throws NoFreeResourceException {
		checkGlobalLoadConditions();
		int initial = contactCursor;
		ContactSet resources = getResources();
		boolean incompatibleConstraints = true;
		while (true) {
			if (checkConstraints(resources.get(contactCursor), t)) {
				incompatibleConstraints = false;
				if (checkLoad(resources.get(contactCursor))) {
					break;
				}
			}
			incContactCursor();
			if (contactCursor == initial) {
				if (incompatibleConstraints) {
					throw new NoSuchResourceException();
				}
				else {
					throw new NoFreeResourceException("No free hosts available");
				}
			}
		}
		BoundContact contact = getResources().get(contactCursor);
		if (logger.isDebugEnabled()) {
			logger.debug("Contact: " + contact);
		}
		incContactCursor();
		return contact;
	}

	private int incContactCursor() {
		contactCursor++;
		if (contactCursor >= getResources().size()) {
			contactCursor = 0;
		}
		return contactCursor;
	}

	public void setProperty(String name, Object value) {
		if (name.equalsIgnoreCase("showTaskList")) {
			logger.debug("Scheduler: setting showTaskList to " + value);
			setTaskList(TypeUtil.toBoolean(value));
		}
		else {
			super.setProperty(name, value);
		}
	}

	public String[] getPropertyNames() {
		return propertyNames;
	}
	
	

	@Override
	public void submitBoundToServices(Entry e, Contact[] contacts, Service[] services)
			throws TaskSubmissionException {
		super.submitBoundToServices(e, contacts, services);
		if (showTaskList) {
			tl.update(getJobQueue().size(), getRunning());
		}
	}

	@Override
	public void statusChanged(StatusEvent se, Entry e) {
		super.statusChanged(se, e);
		if (showTaskList) {
			tl.update(getJobQueue().size(), getRunning());
		}
	}
}