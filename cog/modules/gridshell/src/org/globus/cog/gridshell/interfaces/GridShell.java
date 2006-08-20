/*
 * GridShell is is the controller and has access to a gui and an application
 */
package org.globus.cog.gridshell.interfaces;

import javax.swing.Action;

/**
 * 
 */
public interface GridShell extends Program {
	/**
	 * The default prompt for a grid shell
	 */
	static final String DEFAULT_PROMPT = ">> ";
	/**
	 * Switches to an accepting command state
	 */
	void acceptCommandState();
	/**
	 * Gets the GUI Component associated with this GridShell
	 * @return
	 */
	GridShellSwingGUI getGUI();
	/**
	 * Gets the App Component associated with this GridShell
	 * @return
	 */
	GridShellApp getApplication();		
	/**
	 * Switches to a processing state
	 */
	void processCommandState();
	
	/**
	 * Allows applications to be set by new shells
	 * @param nApp - the new application
	 */
	void setApplication(GridShellApp nApp);
	/**
	 * Returns the action invoked to close the GridShell
	 * @return
	 */
	Action getCloseAction();
}
