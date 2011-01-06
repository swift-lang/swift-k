
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------
    
package org.globus.cog.gui.setup.components;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.globus.cog.gui.util.GridPosition;

public class LicenseComponent
	extends AbstractSetupComponent
	implements SetupComponent, ChangeListener {

	private JCheckBox agree;

	public LicenseComponent() {
		super("License Agreement", "text/setup/license.txt");

		JLabel label = new JLabel("License");

		add(label, new GridPosition(0, 0));

		agree = new JCheckBox("I agree to these terms and conditions");
		agree.addChangeListener(this);

		add(agree, new GridPosition(2, 0));
	}

	public boolean verify() {
		if (!super.verify()) {
			return false;
		}
		return agree.isSelected();
	}

	public void stateChanged(ChangeEvent e) {
		setCompleted(true);
		fireComponentStatusChangedEvent();
	}
}
