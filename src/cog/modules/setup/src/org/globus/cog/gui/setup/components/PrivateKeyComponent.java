
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------
    
package org.globus.cog.gui.setup.components;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.cert.X509Certificate;

import org.globus.cog.gui.setup.controls.FileInputControl;
import org.globus.cog.gui.setup.controls.PasswordInput;
import org.globus.cog.gui.setup.util.ButtonWithState;
import org.globus.cog.gui.util.GridContainer;
import org.globus.cog.gui.util.GridPosition;
import org.globus.cog.gui.util.SimpleGridLayout;
import org.globus.common.CoGProperties;
import org.globus.gsi.bc.BouncyCastleOpenSSLKey;
import org.globus.tools.ui.config.Configure;

public class PrivateKeyComponent
	extends AbstractSetupComponent
	implements SetupComponent, ActionListener {

	private FileInputControl privateKeyFile;
	private ButtonWithState test;
	private CoGProperties properties;

	public PrivateKeyComponent(CoGProperties properties) {
		super("Private Key", "text/setup/private_key.txt");
		GridContainer panel = new GridContainer(1, 2);
		panel.setPreferredSize(new Dimension(SimpleGridLayout.Expand, 54));

		this.properties = properties;
		privateKeyFile = new FileInputControl(properties.getUserKeyFile());
		panel.add(privateKeyFile);
		test = new ButtonWithState("Test");
		test.setAlignmentX(1);
		test.setPreferredSize(new Dimension(100, 24));
		test.addActionListener(this);
		panel.add(test);

		add(panel, new GridPosition(2, 0));
	}

	public boolean verify() {
		if (!super.verify()) {
			return false;
		}

		if (!privateKeyFile.exists()) {
			setErrorMessage("The specified private key file does not exist.");
			return false;
		}
		if (!privateKeyFile.isFile()) {
			setErrorMessage("The specified private key points to a directory.");
			return false;
		}
		return true;
	}

	public void enter() {
		super.enter();
		privateKeyFile.setFileName(properties.getUserKeyFile());
	}

	public boolean leave() {
		if (super.leave()) {
			properties.setUserKeyFile(privateKeyFile.getFileName());
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

	public boolean test() {
		BouncyCastleOpenSSLKey KH = null;
		X509Certificate cert = null;

		try {
			cert = Configure.verifyUserCertificate(properties.getUserCertFile());
		}
		catch (Exception e) {
			setErrorMessage("You need to have a valid certificate in order to be able to test the private key.");
			return false;
		}

		try {
			KH = new BouncyCastleOpenSSLKey(privateKeyFile.getFileName());
		}
		catch (Exception ee) {
			setErrorMessage(
				"Cannot load the private key. Check if the file you specified actually points"
					+ " to your private key.");
			return false;
		}

		if (KH.isEncrypted()) {

			PasswordInput PI =
				new PasswordInput("Private key password", "Please enter the private key password:");
			String pwd = PI.getPassword();
			if (!PI.wasOk()) {
				setErrorMessage("The verificatio cannot proceed without the private key password.");
				return false;
			}

			if (pwd == null) {
				setErrorMessage("You need to specify the correct password for your private key.");
				return false;
			}

			try {
				KH.decrypt(pwd);
			}
			catch (GeneralSecurityException e1) {
				setErrorMessage("The supplied password in invalid for this key.");
				return false;
			}

			//try to sign a simple thing
			Signature sign;
			try {
				sign = Signature.getInstance(cert.getSigAlgName());
				sign.initSign(KH.getPrivateKey());
				byte[] SV = { 1, 2, 3, 4 };
				sign.update(SV);
				byte[] SVSig = sign.sign();
				//done signing; now verify it
				sign.initVerify(cert);
				sign.update(SV);
				if (!sign.verify(SVSig)) {
					setErrorMessage("The specified private key does not match the specified certificate.");
					return false;
				}
				else {
					return true;
				}
			}
			catch (Exception e) {
				setErrorMessage("An error occured while trying to sign a message.");
				return false;
			}

		}
		return true;
	}
}
