//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 18, 2006
 */
package org.globus.cog.karajan.scheduler;

import java.util.List;

import org.globus.cog.karajan.util.BoundContact;

// I'm terrible with naming classes
public interface ResourceConstraintChecker {
	/**
	 * Checks if a resource meets the given task constraints
	 * 
	 * @param resource
	 *            the resource to be checked
	 * @param tc
	 *            the constraints to check the resource against
	 * 
	 * @return <code>true</code> if the resouce meets the constraints;
	 *         <code>false</code> otherwise
	 */
	boolean checkConstraints(BoundContact resource, TaskConstraints tc);

	/**
	 * Identifies the resouces in a list meeting the given task constraints
	 * 
	 * @param resources
	 *            a list of BoundContact objects
	 * @param tc
	 *            the task constraints
	 * @return the subset of contacts meeting the given constraints
	 * 
	 * @see BoundContact
	 */
	List checkConstraints(List resources, TaskConstraints tc);
}
