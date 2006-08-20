/*
 * The Graphical component for GridShell
 */
package org.globus.cog.gridshell.interfaces;

import java.awt.Cursor;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

/**
 * 
 */
public interface GridShellSwingGUI extends PropertyChangeListener, GridShellGUI {
	public static final Cursor PROCESS_CURSOR = new Cursor(Cursor.WAIT_CURSOR);
	public static final Cursor ACCEPT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
	
	/**
	 * Returns the command field
	 * @return
	 */
	JTextComponent getCommandField();
	/**
	 * Returns the history field
	 * @return
	 */	
	JTextComponent getHistoryField();
	/**
	 * Gets the JComponent associated with the GUI, this is what you want to display
	 * @return
	 */
	JComponent getJComponent();
}