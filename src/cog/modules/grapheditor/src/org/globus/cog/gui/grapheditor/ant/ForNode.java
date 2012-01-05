
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.ant;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Timer;

import org.apache.tools.ant.Project;

/**
 * Rendered for a "for" loop
 */

public class ForNode extends SerialNode
	implements
		PropertyChangeListener,
		ActionListener {
	private Timer timer;
	private Project antProject;

	public ForNode() {
		setComponentType("for");
		loadIcon("images/ant-for.png");
		timer = new Timer(250, this);
		addPropertyChangeListener(this);
	}

	public void setAntProject(Project antProject) {
		this.antProject = antProject;
	}

	public Project getAntProject() {
		return antProject;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == timer) {
			if (getStatus().intValue() == STATUS_RUNNING) {
				/*
				 * if ((getLabel() != null) && (antProject != null)){ String
				 * name = (String) getProperty("name").getValue();
				 * getLabel().setText(name+" = "+antProject.getProperty(name));
				 * firePropertyChange("none really");
				 */
			}
		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals("status")) {
			if (getStatus().intValue() == STATUS_RUNNING) {
				timer.start();
			}
			else {
				timer.stop();
			}
		}
	}
}
