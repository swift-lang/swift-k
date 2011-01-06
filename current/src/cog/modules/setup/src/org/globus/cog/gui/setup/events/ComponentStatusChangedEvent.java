
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.events;

import org.globus.cog.gui.setup.components.SetupComponent;

public class ComponentStatusChangedEvent extends ComponentEvent {
	public ComponentStatusChangedEvent(SetupComponent source) {
		super(source);
	}
}
