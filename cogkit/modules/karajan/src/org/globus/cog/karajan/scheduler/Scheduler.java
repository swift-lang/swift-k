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

import java.util.List;

import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.util.Contact;
import org.globus.cog.karajan.util.ContactSet;
import org.globus.cog.karajan.util.TaskHandlerWrapper;

public interface Scheduler {
	
	public static class Entry {
		public final Task task;
		
		public Object constraints;
		public TaskHandler handler;
		public Contact[] contacts;
		public StatusListener listener; 
		
		public Entry(Task task) {
			this.task = task;
		}
		
		public Entry(Task task, Object constraints) {
			this.task = task;
			this.constraints = constraints;
		}
		
		public Entry(Task task, Object constraints, StatusListener listener) {
			this.task = task;
			this.constraints = constraints;
			this.listener = listener;
		}
	}


	/**
	 * Adds a task to the queue.
	 * 
	 * @param constraints
	 *            Can be used to specify constraints under which the task should
	 *            be scheduler. While the semantics of the constraints are left
	 *            to the implementation, such an implementation should be able
	 *            to handle at least constraints of the type Contact.
	 * @see org.globus.cog.karajan.util.Contact
	 */
	void enqueue(Task task, Object constraints, StatusListener listener);

	/**
	 * Attempt to allocate a host. The returned object should be used later by
	 * the requestor as a constraint while enqueuing a task.
	 */
	Contact allocateContact() throws NoFreeResourceException;
	
	Contact allocateContact(Object constraints) throws NoFreeResourceException;
	
	/**
	 * Can be used to tell the scheduler that a previously allocated contact
	 * (using allocateContact()) is not used any more.
	 */
	void releaseContact(Contact sc);

	/**
	 * Sets the set of resources that the scheduler will use
	 * 
	 * @see org.globus.cog.karajan.util.ContactSet
	 */
	void setResources(ContactSet resources);

	/**
	 * Returns the set of resources that the scheduler knows about
	 */
	ContactSet getResources();

	/**
	 * Makes the scheduler aware of a task handler implementation. The scheduler
	 * can then make use of any of the task handlers that were added.
	 * 
	 * @see org.globus.cog.karajan.util.TaskHandlerWrapper
	 */
	void addTaskHandler(TaskHandlerWrapper taskHandler);

	/**
	 * Returns a list of all the task handlers that were added to the scheduler
	 */
	List<TaskHandlerWrapper> getTaskHandlers();

	/**
	 * Sets a scheduler property. The supported property names can be queried
	 * using the <tt>getPropertyNames</tt> method
	 */
	void setProperty(String name, Object value);

	String[] getPropertyNames();

	/**
	 * Prematurely terminates a task that is either enqueued or running
	 */
	void cancelTask(Task task);
	
	void cancelTask(Task t, String message);
	
	/**
	 * Adds a task transformer to this scheduler. A task transformer
	 * allows modifying of various task parameters after the task
	 * has been fully resolved.
	 */
	void addTaskTransformer(TaskTransformer transformer);
	
	/**
	 * Allows handling task failures at the scheduler level
	 */
	void addFailureHandler(FailureHandler handler);

	Object getConstraints(Task t);
	
	void start();
}
