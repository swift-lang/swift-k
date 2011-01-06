//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.desktop.panels;

import java.awt.Dimension;

import org.globus.cog.gridface.interfaces.GridCommand;

public class JobMonitorPanel extends FormPanelSet{
	private OutputPanel outputPanel=null;
	private ErrorPanel errorPanel=null;
	
	public JobMonitorPanel(GridCommand command) {
		outputPanel = new OutputPanel(command);
		errorPanel = new ErrorPanel(command);
		
		this.addForm(outputPanel);
		this.addForm(errorPanel);
		this.panelSet.setPreferredSize(new Dimension(500,200));
		//this.setPreferredSize(new Dimension(500,200));
	}
}

