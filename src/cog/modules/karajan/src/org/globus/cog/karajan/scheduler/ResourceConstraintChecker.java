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

//I'm terrible with naming classes
public interface ResourceConstraintChecker {
	boolean checkConstraints(BoundContact resource, TaskConstraints tc);
	
	List checkConstraints(List resources, TaskConstraints tc);
}
