// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.interfaces;

import org.globus.cog.abstraction.impl.common.taskgraph.ChangeEvent;

/**
 * This interface provides the semantics to listen to the changes made to the
 * structure of a {@link TaskGraph}object. For example, addition and removal of
 * <code>Task</code> s to a <code>TaskGraph</code> is notified to objects
 * implementing this interface.
 */
public interface ChangeListener {
    public void graphChanged(ChangeEvent event) throws Exception;
}