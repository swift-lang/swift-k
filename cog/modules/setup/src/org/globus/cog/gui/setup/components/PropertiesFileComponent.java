// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;

import org.globus.cog.gui.setup.controls.FileInputControl;
import org.globus.cog.gui.setup.controls.TextEditor;
import org.globus.cog.gui.setup.util.Callback;
import org.globus.cog.gui.util.GridContainer;
import org.globus.cog.gui.util.GridPosition;
import org.globus.cog.gui.util.SimpleGridLayout;
import org.globus.common.CoGProperties;

public class PropertiesFileComponent extends AbstractSetupComponent implements SetupComponent,
		ActionListener, Callback {

	private FileInputControl propertiesFile;
	private CoGProperties properties;
	private JButton edit;

	public PropertiesFileComponent(CoGProperties properties) {
		super("Properties File", "text/setup/properties_file.txt");
		this.properties = properties;

		GridContainer panel = new GridContainer(1, 2);
		panel.setPreferredSize(SimpleGridLayout.Expand, 54);

		edit = new JButton("Edit");
		edit.setAlignmentX(RIGHT_ALIGNMENT);
		edit.addActionListener(this);

		propertiesFile = new FileInputControl(CoGProperties.configFile, false);
		propertiesFile.setPreferredSize(SimpleGridLayout.Expand, 26);

		panel.add(propertiesFile);
		panel.add(edit);

		add(panel, new GridPosition(2, 0));
	}

	public void enter() {
		super.enter();
		propertiesFile.setFileName(CoGProperties.configFile);
		ByteArrayOutputStream OS = new ByteArrayOutputStream();
		try {
			properties.store(OS, "Java CoG Kit Configuration File");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean verify() {
		if (!super.verify()) {
			return false;
		}
		return true;
	}

	public boolean leave() {
		// override to prevent the component from showing it has completed
		boolean sl = super.leave();

		setCompleted(false);
		return sl;
	}

	public boolean finish() {
		if (super.finish()) {
			if (propertiesFile.exists()) {
				setErrorMessage("The specified file already exists.");
				if (!confirmBogusSettings()) {
					return false;
				}
				if (!propertiesFile.canWrite()) {
					setErrorMessage("The specified file is marked read only. Please change the permissions on the file to allow"
							+ " writing.\nIf you choose to continue without changing the permissions on the file, your settings will not be saved.");
					if (!confirmBogusSettings()) {
						return false;
					}
				}
			}
			try {
				String fileName = propertiesFile.getFileName();
				File pdir = new File(fileName).getParentFile();
				if (pdir.exists() && !pdir.isDirectory()) {
					setErrorMessage("Cannot save properties file. " + pdir + " is not a directory.");
					return confirmBogusSettings();
				}
				if (!pdir.exists() && !pdir.mkdirs()) {
					setErrorMessage("Could not create " + pdir + " directory.");
					return confirmBogusSettings();
				}
				else {
					properties.save(propertiesFile.getFileName());
					setCompleted(true);
					return true;
				}
			}
			catch (IOException e) {
				setErrorMessage("Could not save the properties. Try specifying another file.");
				displayErrorMessage();
				return false;
			}
		}
		else {
			return confirmBogusSettings();
		}
	}

	public boolean canFinish() {
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == edit) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				properties.store(os, "Java CoG Kit Configuration File");
			}
			catch (IOException e1) {
				setErrorMessage("Cannot create edit buffer.");
				displayErrorMessage();
				return;
			}
			new TextEditor(os.toString(), this);
		}
	}

	public void callback(Object source, Object data) {
		if (source instanceof TextEditor) {
			TextEditor editor = (TextEditor) source;
			String edited = (String) data;
			editor.dispose();
			ByteArrayInputStream is = new ByteArrayInputStream(edited.getBytes());
			try {
				properties.load(is);
			}
			catch (IOException e) {
				setErrorMessage("Cannot parse the modified file. Reverting to initial values.");
				displayErrorMessage();
				ByteArrayInputStream is2 = new ByteArrayInputStream(
						editor.getInitialText().getBytes());
				try {
					properties.load(is2);
				}
				catch (IOException ee) {
					setErrorMessage("Cannot parse initial settings.\nPlease send us a detailed report about this");
					displayErrorMessage();
				}
			}
		}
	}
}
