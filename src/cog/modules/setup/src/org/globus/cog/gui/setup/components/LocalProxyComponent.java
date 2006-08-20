
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------
    
package org.globus.cog.gui.setup.components;

import java.awt.Dimension;

import org.globus.cog.gui.setup.controls.FileInputControl;
import org.globus.cog.gui.util.GridContainer;
import org.globus.cog.gui.util.GridPosition;
import org.globus.cog.gui.util.SimpleGridLayout;
import org.globus.common.CoGProperties;

public class LocalProxyComponent extends AbstractSetupComponent implements SetupComponent {

	private FileInputControl localProxyFile;
	private CoGProperties properties;

	public LocalProxyComponent(CoGProperties properties) {
		super("Local Proxy", "text/setup/local_proxy.txt");
		this.properties = properties;

		GridContainer panel = new GridContainer(1, 1);
		panel.setPreferredSize(new Dimension(SimpleGridLayout.Expand, 54));

		localProxyFile = new FileInputControl(properties.getProxyFile());
		localProxyFile.setPreferredSize(SimpleGridLayout.Expand, 26);
		panel.add(localProxyFile);

		add(panel, new GridPosition(2, 0));
	}

	public boolean verify() {
		if (!super.verify()) {
			return false;
		}
		if (!localProxyFile.exists()) {
			try {
				localProxyFile.createNewFile();
				return true;
			}
			catch (Exception e) {
				setErrorMessage(
					"The specified file does not exist and it could not be created.\n"
						+ "Please check if you have the necessary permisions.");
				return false;
			}
		}
		if (!localProxyFile.isFile()) {
			setErrorMessage("The specified file is a directory.");
			return false;
		}
		return true;
	}

	public void enter() {
		super.enter();
		localProxyFile.setFileName(properties.getProxyFile());
	}

	public boolean leave() {
		if (super.leave()) {
			properties.setProxyFile(localProxyFile.getFileName());
			return true;
		}
		else {
			return false;
		}
	}
}
