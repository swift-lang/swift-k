
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.setup.util;

import org.globus.cog.gui.setup.components.SetupComponent;
import org.globus.cog.gui.setup.controls.ComponentListItem;

public class ComponentLabelBridge {
	private SetupComponent SC;
	private ComponentListItem CLI;

	public ComponentLabelBridge(SetupComponent SC) {
		this.SC = SC;
		this.CLI = new ComponentListItem(SC.getTitle());
	}

	public SetupComponent getSetupComponent() {
		return SC;
	}

	public ComponentListItem getComponentListItem() {
		return CLI;
	}
}
