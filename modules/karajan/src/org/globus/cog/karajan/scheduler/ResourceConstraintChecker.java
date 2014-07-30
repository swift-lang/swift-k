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
