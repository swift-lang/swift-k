
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.controls;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

import org.globus.cog.gui.setup.util.HSpacer;
import org.globus.cog.gui.util.GridContainer;
import org.globus.cog.gui.util.SimpleGridLayout;

/**
 *  Simple control for file specification. Includes a browse button and a viewer
 *  Also wraps some of the functionality of java.io.File
 */
public class FileInputControl extends GridContainer implements ActionListener {
	private JTextField path;
	private JButton browse, view;
	private String defaultPath;

	public FileInputControl(String initialPath, boolean viewable) {
		super(1, 4);

		path = new JTextField(initialPath);
		defaultPath = new File(initialPath).getParent();
		path.setPreferredSize(new Dimension(SimpleGridLayout.Expand, 24));
		path.setEditable(false);

		browse = new JButton("...");
		browse.addActionListener(this);
		browse.setPreferredSize(new Dimension(40, 24));

		view = new JButton("View");
		view.addActionListener(this);
		view.setPreferredSize(new Dimension(80, 24));
		view.setAlignmentX(1);

		add(new HSpacer(10));
		add(path);
		add(browse);
		if (viewable) {
			add(view);
		}
	}

	public FileInputControl(String initialPath) {
		this(initialPath, true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == browse) {
			String Default = getFileName();

			if (!new File(Default).exists()) {
				Default = defaultPath;
			}

			JFileChooser JF = new JFileChooser(Default);

			JF.setFileSelectionMode(JFileChooser.FILES_ONLY);
			JF.setFileHidingEnabled(false);

			int ret = JF.showOpenDialog(this);

			if (ret == JFileChooser.APPROVE_OPTION) {
				path.setText(JF.getSelectedFile().getAbsolutePath());
				//notify the parent that the thing has changed
				firePropertyChange("FileName", "", path.getText());
			}

		}
		else if (e.getSource() == view) {
			TextFileViewer FW = new TextFileViewer(null, path.getText());

			FW.showDialog();
		}
	}

	public void setEnabled(boolean enabled) {
		path.setEnabled(enabled);
		browse.setEnabled(enabled);
		view.setEnabled(enabled);
	}

	public String getFileName() {
		return path.getText();
	}

	public boolean exists() {
		return new File(getFileName()).exists();
	}

	public boolean isFile() {
		return new File(getFileName()).isFile();
	}

	public boolean canWrite() {
		return new File(getFileName()).canWrite();
	}

	public synchronized void createNewFile() throws IOException, SecurityException {
		new File(getFileName()).createNewFile();
	}

	public void setFileName(String FileName) {
		path.setText(FileName);
	}
}
