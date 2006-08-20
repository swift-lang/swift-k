// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.components;

import java.awt.Dimension;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.globus.cog.gui.util.RegistrationPanel;
import org.globus.cog.gui.util.SimpleGridLayout;

public class RegistrationComponent extends AbstractSetupComponent implements SetupComponent,
		ChangeListener {

	private RegistrationPanel registration;

	public RegistrationComponent() {
		super("Registration", null);

		registration = new RegistrationPanel();
		registration.setPreferredSize(new Dimension(SimpleGridLayout.Expand,
				SimpleGridLayout.Expand));
		add(registration);
	}

	public boolean verify() {
		if (!super.verify()) {
			return false;
		}
		try {
			registration.submit(false);
			return true;
		}
		catch (Exception e) {
			setErrorMessage("Could not submit registration information: " + e.toString());
			return false;
		}
	}

	public void stateChanged(ChangeEvent e) {
		setCompleted(true);
		fireComponentStatusChangedEvent();
	}
}
