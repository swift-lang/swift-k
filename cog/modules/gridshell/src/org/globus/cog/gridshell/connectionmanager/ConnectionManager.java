/*
 *
 */
package org.globus.cog.gridshell.connectionmanager;

import org.globus.cog.gridshell.tasks.StartTask;

/**
 * 
 */
public interface ConnectionManager {
    /**
     * Returns a default connection
     * @return
     */
	StartTask getDefaultConnection();
	/**
	 * Determines if the default connection is current
	 * @return
	 */
	boolean isCurrentDefaultConnection();
	/**
	 * Gets the current connection
	 * @return
	 */
	StartTask getCurrentConnection();
	/**
	 * Adds a connection to the top of the stack
	 * @param connection
	 */
	void push(StartTask connection);
	/**
	 * Removes and returns the last created connection
	 * @return
	 */
	StartTask pop();
}
