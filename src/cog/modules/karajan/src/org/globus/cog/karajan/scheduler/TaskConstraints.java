// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 1, 2004
 *
 */
package org.globus.cog.karajan.scheduler;

import java.util.Collection;

public interface TaskConstraints {
    
	Object getConstraint(String name);

	Collection<String> getConstraintNames();
}
