// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.interfaces;

import org.globus.cog.abstraction.impl.common.StatusEvent;

/**
 * This interface represents a listener that gets notified every time the the
 * status of {@link Task}changes.
 */
public interface StatusListener {
    public void statusChanged(StatusEvent event);
}