
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.events;

import java.util.EventListener;

public interface ComponentStatusChangedListener extends EventListener {
	public void componentStatusChanged(ComponentStatusChangedEvent e);
}
