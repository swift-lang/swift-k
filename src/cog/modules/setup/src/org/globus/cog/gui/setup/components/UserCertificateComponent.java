
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------
    
package org.globus.cog.gui.setup.components;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.globus.cog.gui.setup.controls.FileInputControl;
import org.globus.cog.gui.util.GridContainer;
import org.globus.cog.gui.util.GridPosition;
import org.globus.cog.gui.util.SimpleGridLayout;
import org.globus.common.CoGProperties;
import org.globus.tools.ui.config.Configure;

public class UserCertificateComponent
	extends AbstractSetupComponent
	implements SetupComponent, ActionListener {

	private FileInputControl certFile;
	private CoGProperties properties;

	public UserCertificateComponent(CoGProperties properties) {
		super("User Certificate", "text/setup/user_certificate.txt");
		this.properties = properties;
		certFile = new FileInputControl(properties.getUserCertFile());
		certFile.setPreferredSize(new Dimension(SimpleGridLayout.Expand, 26));
		certFile.setAlignmentX(1);

		GridContainer panel = new GridContainer(1, 1);
		panel.setPreferredSize(new Dimension(SimpleGridLayout.Expand, 54));

		panel.add(certFile);
		panel.setAlignmentX(1);

		add(panel, new GridPosition(2, 0));
	}

	public boolean verify() {
		if (!super.verify()) {
			return false;
		}
		try {
			Configure.verifyUserCertificate(certFile.getFileName());
		}
		catch (Exception e) {
			setErrorMessage("The specified certificate is invalid.");
			return false;
		}
		return true;
	}

	public void enter() {
		super.enter();
		certFile.setFileName(properties.getUserCertFile());
	}

	public boolean leave() {
		if (super.leave()) {
			properties.setUserCertFile(certFile.getFileName());
			return true;
		}
		else {
			return false;
		}
	}

	public void actionPerformed(ActionEvent e) {
	}
}
