/*
 * Created on Mar 16, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.globus.cog.gridshell.interfaces;

import java.beans.PropertyChangeListener;

/**
 * 
 */
public interface GridShellGUI extends PropertyChangeListener {
	/**
	 * Get the prompt plus the commandline value
	 * @return
	 */
	String getCommandValue();
	/**
	 * Set the prompt plus commandline value
	 * @param value
	 */
	void setCommandValue(String value);
	/**
	 * Get the history that is displayed
	 * @return
	 */
	String getHistoryValue();
	/**
	 * Append a value to the history
	 * @param value
	 */
	void appendHistoryValue(String value);
	/**
	 * Set the history
	 * @param value
	 */
	void setHistoryValue(String value);
}
