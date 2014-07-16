// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.interfaces;

import org.globus.cog.abstraction.impl.common.task.OutputEvent;

/**
 * This interface represents a listener that gets notified every time the 
 * the contents of {@link org.globus.cog.abstraction.interfaces.Task#getStdOutput()} changes.
 */
public interface OutputListener {
    public void outputChanged(OutputEvent event);
}
