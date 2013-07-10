
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.events;

import java.util.EventObject;

import org.globus.cog.gui.setup.components.SetupComponent;

public class ComponentEvent extends EventObject {

	public ComponentEvent(SetupComponent source) {
		super(source);
	}


	public SetupComponent getComponent() {
		return (SetupComponent) getSource();
	}
}

