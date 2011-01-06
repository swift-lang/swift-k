
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.components;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;

import org.globus.cog.gui.setup.controls.CAControl;
import org.globus.cog.gui.setup.util.ButtonWithState;
import org.globus.cog.gui.util.GridContainer;
import org.globus.cog.gui.util.GridPosition;
import org.globus.cog.gui.util.SimpleGridLayout;
import org.globus.common.CoGProperties;
import org.globus.gsi.CertUtil;

public class CertificateAuthorityComponent
	extends AbstractSetupComponent
	implements SetupComponent, ActionListener {

	private CAControl CA;
	private ButtonWithState test;
	private CoGProperties properties;

	public CertificateAuthorityComponent(CoGProperties properties) {
		super("Certificate Authorities", "text/setup/certificate_authority.txt");
		this.properties = properties;

		GridContainer Panel = new GridContainer(1, 2);
		Panel.setPreferredSize(new Dimension(SimpleGridLayout.Expand, 160));

		CA = new CAControl(properties.getCaCertLocations());

		test = new ButtonWithState("Test");
		test.setAlignmentX(1);
		test.setPreferredSize(new Dimension(100, 24));
		test.addActionListener(this);

		Panel.add(CA);
		//Panel.add(test);

		add(Panel, new GridPosition(2, 0));
	}

	public boolean verify() {

		if (!super.verify()) {
			return false;
		}

		Iterator certIterator = CA.getSelectedFileNames().iterator();
		if (!certIterator.hasNext()) {
			setErrorMessage("You will not be able to connect to Globus services without specifying a certificate authority.");
			return false;
		}
		boolean any = false;
		while (certIterator.hasNext()) {
			File cf = new File((String) certIterator.next());
			if (cf.exists() && cf.isDirectory()) {
				File[] cas = cf.listFiles();
				if (cas.length > 0) {
					any = true;
				}
			}
			if (cf.exists() && cf.isFile()) {
				any = true;
				break;
			}
		}

		if (!any) {
			setErrorMessage("None of the specified CAs exists.");
			return false;
		}

		return true;
	}

	public boolean checkCert(String file) {
		try {
			CertUtil.loadCertificate(file);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	public boolean test() {
		Iterator certIterator = CA.getSelectedFileNames().iterator();
		boolean any = false;
		while (certIterator.hasNext()) {
			String CAName = (String) certIterator.next();
			File CAFile = new File(CAName);
			if (CAFile.isDirectory()) {
				File[] CAS = CAFile.listFiles();
				for (int i = 0; i < CAS.length; i++) {
					if (!CAS[i].getAbsolutePath().endsWith("signing_policy")) {
						if (checkCert(CAS[i].getAbsolutePath())) {
							any = true;
						}
					}
				}
			}
			else {
				if (!checkCert(CAName)) {
					setErrorMessage("The following certificate is invalid: " + CAName);
					return false;
				}
				else {
					any = true;
				}
			}
		}

		if (!any) {
			setErrorMessage("No valid certificate authority was found among the specified files");
			return false;
		}
		return true;
	}

	public void enter() {
		super.enter();
		String certLoc = properties.getCaCertLocations();
		String home = System.getProperty("user.home");
		if (certLoc == null) {
			if (System.getProperty("X509_CERT_DIR") != null) {
				CA.setFileNames(System.getProperty("X509_CERT_DIR"));
				return;
			}
			String certName = new File(home, ".globus/42864e48.0").getAbsolutePath();
			if (checkCert(certName)) {
				CA.setFileNames(certName);
				return;
			}
			String globusLocation = System.getProperty("GLOBUS_LOCATION");
			if (globusLocation != null) {
				String fs = System.getProperty("file.separator");
				certName = globusLocation + fs + "share" + fs + "certificates" + fs + "42864e48.0";
				if (checkCert(certName)) {
					CA.setFileNames(certName);
					return;
				}
			}
		}
		else {
			CA.setFileNames(properties.getCaCertLocations());
		}
	}

	public boolean leave() {
		if (super.leave()) {
			properties.setCaCertLocations(CA.getFileNames());
			return true;
		}
		else {
			return false;
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == test) {
			if (!test()) {
				displayErrorMessage();
				test.setState(ButtonWithState.StateFailed);
			}
			else {
				test.setState(ButtonWithState.StateOk);
			}
		}
	}
}
