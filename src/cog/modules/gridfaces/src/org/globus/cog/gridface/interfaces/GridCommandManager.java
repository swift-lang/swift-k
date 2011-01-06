
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.interfaces;

import org.globus.cog.gridface.interfaces.Scheduler;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;

import java.util.Enumeration;
import org.globus.cog.gridface.impl.desktop.interfaces.*;
/**
 * GridCommandManager is the backend component of gridface guis to
 * access cog-abstraction module.
 */
public interface GridCommandManager extends AccessDesktop {

	/** Execute the grid command. If inBackground is true, then command is executed by a separate thread */
	public Identity execute(GridCommand command, boolean inBackground)
		throws Exception;

	/** Execute the grid command. If there are dependencies, add dependencies. */
	public Identity execute(
		GridCommand command,
		boolean inBackground,
		Enumeration idList)
		throws Exception;

	/** Get the status of the given command */
	public Status getStatus(String id);

	/** Get the status of the given command */
	public Status getStatus(Identity id);

	/** Cancel the command that has the given id */
	public boolean cancel(String id) throws Exception;

	/** Cancel the command that has the given id */
	public boolean cancel(Identity id) throws Exception;

	/** Syspend the command that has the given id, */
	public boolean suspend(Identity id) throws Exception;
	/** Resume the command that has the given id, */
	public boolean resume(Identity id) throws Exception;
	
	
	/** Get a list of all commands submitted to GCM */
	public Enumeration getAllCommands();

	/** Get a list of all commands with current status as UNSUBMITTED */
	public Enumeration getUnsubmittedCommands();

	/** Get a list of all commands with current status as SUBMITTED */
	public Enumeration getSubmittedCommands();

	/** Get a list of all commands with current status as ACTIVE */
	public Enumeration getActiveCommands();

	/** Get a list of all commands with current status as FAILED */
	public Enumeration getFailedCommands();

	/** Get a list of all commands with current status as COMPLETED */
	public Enumeration getCompletedCommands();

	/** Get a list of all commands with current status as SUSPENDED */
	public Enumeration getSuspendedCommands();

	/** Get a list of all commands with current status as CANCELED */
	public Enumeration getCanceledCommands();

	/** Set priority for executing the command with given id */
	public void setPriorities(long id, int priority) throws Exception;

	/** Set a scheduler for scheduling events submitted to GCM */
	public void setScheduler(Scheduler scheduler) throws Exception;

	/** Get the scheduler currently used */
	public Scheduler getScheduler() throws Exception;

	/** Save the state of GCM into an XML file */
	public void toXML(String fileName) throws Exception;

	/** add listener  */
	public void addStatusListener(StatusListener listener);

	/** remove listener  */
	public void removeStatusListener(StatusListener listener);

}
