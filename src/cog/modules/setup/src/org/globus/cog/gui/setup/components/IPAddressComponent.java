
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------
    
package org.globus.cog.gui.setup.components;

import java.awt.Dimension;

import org.globus.cog.gui.setup.controls.IPInputControl;
import org.globus.cog.gui.util.GridContainer;
import org.globus.cog.gui.util.GridPosition;
import org.globus.cog.gui.util.SimpleGridLayout;
import org.globus.common.CoGProperties;

public class IPAddressComponent extends AbstractSetupComponent implements SetupComponent {

	private IPInputControl ip;
	private CoGProperties properties;

	public IPAddressComponent(CoGProperties properties) {
		super("IP Address", "text/setup/ip_address.txt");
		this.properties = properties;

		GridContainer panel = new GridContainer(1, 1);
		panel.setPreferredSize(new Dimension(SimpleGridLayout.Expand, 54));

		ip = new IPInputControl(properties.getIPAddress());
		ip.setPreferredSize(SimpleGridLayout.Expand, 26);
		ip.setAlignmentX(RIGHT_ALIGNMENT);
		panel.add(ip);

		add(panel, new GridPosition(2, 0));
	}

	public boolean verify() {
		if (!super.verify()) {
			return false;
		}
		if (ip.getProbedIP() == null) {
			setErrorMessage(
				"This program could not probe the local IP address.\n"
					+ "This may occur if your system is not connected to the Internet.");
			return false;
		}
		if (!ip.getProbedIP().equals(ip.getIP())) {
			setErrorMessage(
				"The IP address entered is different from the one probed by this program.\n"
					+ "Use the \"Probe\" button to use the one determined by this program.");
			return false;
		}
		return true;
	}

	public void enter() {
		super.enter();
		ip.setIP(properties.getIPAddress());
	}

	public boolean leave() {
		if (super.leave()) {
			properties.setIPAddress(ip.getIP());
			return true;
		}
		else {
			return false;
		}
	}

}
