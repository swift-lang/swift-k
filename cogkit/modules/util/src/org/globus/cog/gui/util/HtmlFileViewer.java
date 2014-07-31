/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;


/**
 *  Displays an html file in a window
 */
public class HtmlFileViewer extends JFrame implements ActionListener {
	private JButton Close;
	boolean quit = false;

	
	/**
	 * Creates and shows an HTML viewer for either a file or a JAR resource 
	 *
	 * @param fileName the HTML file/resource to display
	 */
	public HtmlFileViewer(String fileName) {
		//	super(parent, "File viewer");
		getContentPane().setLayout(new BorderLayout());
		setSize(600, 330);
		UITools.center(null, this);
		JTextPane jTextPane = new JTextPane();
		jTextPane.setEditable(false);
		setTitle("File Tranfer Information  ");
		try {
			URL fileURL = getClass().getClassLoader().getResource(fileName);

			jTextPane.setPage(fileURL);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(
				null,
				"Cannot display Help file" + e.getMessage(),
				"Error!",
				JOptionPane.ERROR_MESSAGE);
			quit = true;
			return;
		}
		JScrollPane Scroller = new JScrollPane(jTextPane);

		Close = new JButton("Close");
		Close.addActionListener(this);

		getContentPane().add(Scroller, BorderLayout.CENTER);
		getContentPane().add(Close, BorderLayout.SOUTH);
		setVisible(true);

	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == Close) {
			setVisible(false);
		}
	}

}
