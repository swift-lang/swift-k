
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 4, 2004
 *
 */
package org.globus.cog.gui.grapheditor.util.swing;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

public class FileInputControl extends JComponent implements ActionListener{
	private JTextField fileName;
	private JButton browse;
	private int selectionModel;
	private String title;
	
	public FileInputControl(int selectionModel, String defaultFile, String title) {
		this.selectionModel = selectionModel;
		this.title = title;
		setLayout(new FlowLayout());
		fileName = new JTextField();
		fileName.setPreferredSize(new Dimension(200, 20));
		fileName.setText(defaultFile);
		add(fileName);
		browse = new JButton("Browse...");
		browse.addActionListener(this);
		add(browse);
	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setName(title);
		fileChooser.setFileSelectionMode(selectionModel);
		File dir;
		if (fileName.getText() != null) {
			dir = new File(fileName.getText());
			if (!dir.isDirectory()) {
				dir = dir.getAbsoluteFile().getParentFile();
			}
		}
		else {
			dir = new File(".").getAbsoluteFile();
		}
		fileChooser.setCurrentDirectory(dir);
		int option = fileChooser.showDialog(this, "Select");
		if (option == JFileChooser.APPROVE_OPTION) {
			fileName.setText(fileChooser.getSelectedFile().getPath());
		}
	}
	
	public String getPath() {
		return fileName.getText();
	}
}
