
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
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.globus.cog.gui.setup.controls.FileInputControl;
import org.globus.cog.gui.util.GridContainer;
import org.globus.cog.gui.util.GridPosition;
import org.globus.cog.gui.util.SimpleGridLayout;
import org.globus.common.CoGProperties;

public class PreviousSetupComponent
	extends AbstractSetupComponent
	implements SetupComponent, ActionListener {

	private JRadioButton previousConfig;
	private JRadioButton newConfig;
	private FileInputControl configPath;
	private CoGProperties properties;

	public PreviousSetupComponent(CoGProperties properties) {
		super("Previous Setup", "text/setup/previous_setup.txt");
		this.properties = properties;
		GridContainer panel = new GridContainer(3, 1);
		panel.setPreferredSize(new Dimension(SimpleGridLayout.Expand, 80));
		ButtonGroup group = new ButtonGroup();
		previousConfig = new JRadioButton("Use a previous configuration");
		group.add(previousConfig);
		previousConfig.addActionListener(this);
		panel.add(previousConfig);
		configPath = new FileInputControl(CoGProperties.configFile);
		configPath.setEnabled(false);
		configPath.setAlignmentX(1);
		panel.add(configPath);
		newConfig = new JRadioButton("Start a new configuration");
		group.add(newConfig);
		newConfig.addActionListener(this);
		panel.add(newConfig);
		add(panel, new GridPosition(2, 0));
	}
	public boolean verify() {
		if (!super.verify()) {
			return false;
		}

		if (previousConfig.isSelected()) {
			File configFile = new File(configPath.getFileName());
			if (!configFile.exists()) {
				setErrorMessage("The configuration file you specified does not exist.");
				return false;
			}
			else {
				return true;
			}
		}
		else {
			return true;
		}
	}
	public void enter() {
		super.enter();
		File cogProps = new File(CoGProperties.configFile);
		if (!previousConfig.isSelected() && !newConfig.isSelected()) {
			if (cogProps.exists()) {
				previousConfig.setSelected(true);
				configPath.setEnabled(true);
			}
			else {
				newConfig.setSelected(true);
			}
		}
	}
	public boolean leave() {
		if (!super.leave()) {
			return false;
		}
		if (previousConfig.isSelected()) {
			File configFile = new File(configPath.getFileName());
			try {
				properties.load(configFile.getAbsolutePath());
				CoGProperties.configFile = configFile.getAbsolutePath();
				return true;
			}
			catch (IOException e) {
				setErrorMessage("The configuration file you specified is invalid.");
				displayErrorMessage();
				return false;
			}
		}
		else {
			CoGProperties defProps = CoGProperties.getDefault();
			if (defProps.getUserCertFile() != null) {
				properties.setUserCertFile(defProps.getUserCertFile());
			}
			if (defProps.getUserKeyFile() != null) {
				properties.setUserKeyFile(defProps.getUserKeyFile());
			}
			if (defProps.getCaCertLocations() != null) {
				properties.setCaCertLocations(defProps.getCaCertLocations());
			}
			if (defProps.getIPAddress() != null) {
				properties.setIPAddress(defProps.getIPAddress());
			}
			return true;
		}
	}

	public void actionPerformed(ActionEvent e) {
		configPath.setEnabled(previousConfig.isSelected());
		fireComponentStatusChangedEvent();
	}
}
