// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.scheduler;

import java.util.List;

import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.util.Contact;
import org.globus.cog.karajan.util.ContactSet;
import org.globus.cog.karajan.util.TaskHandlerWrapper;

public interface Scheduler {

	/**
	 * Adds a task to the queue.
	 * 
	 * @param constraints
	 *            Can be used to specify constraints under which the task should
	 *            be scheduler. While the semantics of the constraints are left
	 *            to the implementation, such an implementation should be able
	 *            to handle at least constraints of the type Contact.
	 * @see org.globus.karajan.util.Contact
	 */
	void enqueue(Task task, Object constraints);

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
	 * Allows the addition of a status listener that will be invoked whenever
	 * the status of the given task changes.
	 */
	void addJobStatusListener(StatusListener l, Task task);

	/**
	 * Removes a status listener added using <tt>addJobStatusListener</tt>
	 */
	void removeJobStatusListener(StatusListener l, Task task);

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
	List getTaskHandlers();

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
	
	Object getConstraints(Task task);
}
